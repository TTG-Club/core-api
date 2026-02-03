package club.ttg.dnd5.domain.token.service;

import club.ttg.dnd5.domain.token.model.TokenBorder;
import club.ttg.dnd5.domain.token.repository.TokenBorderRepository;
import club.ttg.dnd5.domain.token.rest.dto.TokenBorderReorderRequest;
import club.ttg.dnd5.domain.token.rest.dto.TokenBorderResponse;
import club.ttg.dnd5.domain.token.rest.mapper.TokenBorderMapper;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenBorderService
{
    private static final String KEY_PREFIX = "token-borders/";

    /**
     * Отрицательный “далёкий” буфер.
     * Главное — чтобы никогда не пересекался с диапазоном реальных order_index (1..N).
     */
    private static final int BUFFER_ORDER = -1_000_000;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String s3Bucket;

    private final TokenBorderRepository tokenBorderRepository;
    private final TokenBorderMapper tokenBorderMapper;
    private final S3Client s3Client;

    public TokenBorderResponse createAndUpload(final MultipartFile file)
    {
        validateFile(file);

        String key = buildKey(file);

        try
        {
            uploadToS3(file, key);

            TokenBorder border = new TokenBorder();
            border.setUrl(key);

            int nextOrder = tokenBorderRepository.findMaxOrder() + 1;
            border.setOrder(nextOrder);

            return tokenBorderMapper.toResponse(tokenBorderRepository.save(border));
        }
        catch (RuntimeException ex)
        {
            safeDeleteFromS3(key);
            throw ex;
        }
    }

    @Transactional
    public void updateOrder(final TokenBorderReorderRequest request)
    {
        tokenBorderRepository.lockTokenBorderReorder();

        TokenBorder border = getById(request.getId());

        int currentOrder = border.getOrder();
        int maxOrder = tokenBorderRepository.findMaxOrder();
        int desiredOrder = clamp(request.getOrder(), maxOrder);

        if (desiredOrder == currentOrder)
        {
            return;
        }

        // 1) освобождаем текущую позицию, убрав перемещаемую рамку в буфер
        tokenBorderRepository.moveToBuffer(border.getId(), BUFFER_ORDER);

        if (desiredOrder < currentOrder)
        {
            // диапазон, который надо сдвинуть вверх: [desiredOrder, currentOrder - 1]
            int to = currentOrder - 1;

            tokenBorderRepository.moveRangeToNegative(desiredOrder, to);
            tokenBorderRepository.restoreRangeShiftUp(desiredOrder, to);
        }
        else
        {
            // диапазон, который надо сдвинуть вниз: [currentOrder + 1, desiredOrder]
            int from = currentOrder + 1;

            tokenBorderRepository.moveRangeToNegative(from, desiredOrder);
            tokenBorderRepository.restoreRangeShiftDown(from, desiredOrder);
        }

        // 2) ставим рамку на целевую позицию
        tokenBorderRepository.updateOrder(border.getId(), desiredOrder);
    }

    @Transactional
    public void delete(final UUID id)
    {
        tokenBorderRepository.lockTokenBorderReorder();

        TokenBorder border = getById(id);
        String s3Key = extractKeyFromUrl(border.getUrl());

        // Сначала S3: если не получилось — БД не трогаем.
        deleteFromS3(s3Key);

        int deletedOrder = border.getOrder();
        int maxOrder = tokenBorderRepository.findMaxOrder();

        // 1) уводим удаляемую запись в буфер, чтобы освободить deletedOrder и убрать конфликты
        tokenBorderRepository.moveToBuffer(border.getId(), BUFFER_ORDER);

        // 2) удаляем запись (у неё уже уникальный order_index в буфере)
        tokenBorderRepository.deleteById(border.getId());

        // 3) сдвигаем хвост (deletedOrder+1 .. maxOrder) вниз на 1 через отрицательный буфер
        if (deletedOrder < maxOrder)
        {
            int from = deletedOrder + 1;

            tokenBorderRepository.moveRangeToNegative(from, maxOrder);
            tokenBorderRepository.restoreRangeShiftDown(from, maxOrder);
        }
    }

    public Collection<TokenBorderResponse> findAll()
    {
        return tokenBorderRepository.findAll(Sort.by(Sort.Direction.ASC, "order"))
                .stream()
                .map(tokenBorderMapper::toResponse)
                .toList();
    }

    private TokenBorder getById(final UUID id)
    {
        return tokenBorderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Рамка не найдена: " + id));
    }

    private int clamp(final int value, final int max)
    {
        if (value < 1)
        {
            return 1;
        }
        return Math.min(value, max);
    }

    private void uploadToS3(final MultipartFile file, final String key)
    {
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank())
        {
            contentType = "application/octet-stream";
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Bucket)
                .key(key)
                .contentType(contentType)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        try (InputStream inputStream = file.getInputStream())
        {
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
        }
        catch (IOException exception)
        {
            throw new IllegalStateException("Failed to read uploaded file", exception);
        }
    }

    private void safeDeleteFromS3(final String key)
    {
        try
        {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Bucket)
                    .key(key)
                    .build());
        }
        catch (Exception ignored)
        {
            // cleanup best-effort
        }
    }

    private void deleteFromS3(final String key)
    {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(s3Bucket)
                .key(key)
                .build());
    }

    private String buildKey(final MultipartFile file)
    {
        String extension = extractExtension(file.getOriginalFilename());
        if (extension.isBlank())
        {
            extension = "webp";
        }

        return "/s3/" + KEY_PREFIX + UUID.randomUUID() + "." + extension;
    }

    private void validateFile(final MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("Требуется файл");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
        {
            throw new IllegalArgumentException("Загружены могут быть только изображения");
        }
    }

    private String extractExtension(final String filename)
    {
        if (filename == null)
        {
            return "";
        }

        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1)
        {
            return "";
        }

        return filename.substring(dot + 1).toLowerCase();
    }

    private String extractKeyFromUrl(final String url)
    {
        if (url == null || url.isBlank())
        {
            throw new IllegalArgumentException("TokenBorder url is empty");
        }

        URI uri = URI.create(url);
        String path = uri.getPath();
        if (path == null)
        {
            throw new IllegalArgumentException("Invalid url: " + url);
        }

        String expectedPrefix = "/" + s3Bucket + "/";
        if (!path.startsWith(expectedPrefix))
        {
            int idx = path.indexOf(expectedPrefix);
            if (idx < 0)
            {
                throw new IllegalArgumentException("Url does not match bucket '" + s3Bucket + "': " + url);
            }
            path = path.substring(idx);
        }

        String key = path.substring(expectedPrefix.length());
        if (key.isBlank())
        {
            throw new IllegalArgumentException("Could not extract S3 key from url: " + url);
        }

        return key;
    }
}
