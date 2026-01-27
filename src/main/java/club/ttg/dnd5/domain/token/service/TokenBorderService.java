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
import org.springframework.transaction.support.TransactionTemplate;
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

    @Value("${spring.cloud.aws.s3.bucket}")
    private String s3Bucket;

    @Value("${spring.cloud.aws.s3.endpoint}")
    private String s3Endpoint;

    private final TokenBorderRepository tokenBorderRepository;
    private final TokenBorderMapper tokenBorderMapper;
    private final S3Client s3Client;
    private final TransactionTemplate transactionTemplate;

    public TokenBorderResponse createAndUpload(final MultipartFile file)
    {
        validateFile(file);

        String key = buildKey(file);
        String url = buildPublicUrl(key);

        try
        {
            uploadToS3(file, key);

            TokenBorder border = new TokenBorder();
            border.setUrl(url);

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

        int bufferOrder = -1_000_000; // достаточно далеко
        tokenBorderRepository.moveToBuffer(border.getId(), bufferOrder);

        if (desiredOrder < currentOrder)
        {
            int to = currentOrder - 1;

            tokenBorderRepository.moveRangeToNegative(desiredOrder, to);
            tokenBorderRepository.restoreRangeShiftUp(desiredOrder, to);
        }
        else
        {
            // нужно сдвинуть вниз диапазон [currentOrder+1, desiredOrder]
            int from = currentOrder + 1;

            tokenBorderRepository.moveRangeToNegative(from, desiredOrder);
            tokenBorderRepository.restoreRangeShiftDown(from, desiredOrder);
        }

        tokenBorderRepository.updateOrder(border.getId(), desiredOrder);
    }

    @Transactional
    public void delete(final UUID id)
    {
        TokenBorder border = getById(id);
        String s3Key = extractKeyFromUrl(border.getUrl());

        deleteFromS3(s3Key);

        transactionTemplate.executeWithoutResult(status ->
        {
            tokenBorderRepository.lockTokenBorderReorder();

            TokenBorder lockedBorder = getById(id);
            int deletedOrder = lockedBorder.getOrder();

            tokenBorderRepository.delete(lockedBorder);
            tokenBorderRepository.shiftAfterDelete(deletedOrder);
        });
    }

    private String extractKeyFromUrl(final String url)
    {
        if (url == null || url.isBlank())
        {
            throw new IllegalArgumentException("TokenBorder url is empty");
        }

        URI uri = URI.create(url);
        String path = uri.getPath(); // "/<bucket>/<key>"
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

    private void deleteFromS3(final String key)
    {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(s3Bucket)
                .key(key)
                .build());
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

    private String buildKey(final MultipartFile file)
    {
        String extension = extractExtension(file.getOriginalFilename());
        if (extension.isBlank())
        {
            extension = "webp";
        }

        return KEY_PREFIX + UUID.randomUUID() + "." + extension;
    }

    private String buildPublicUrl(final String key)
    {
        String endpoint = trimTrailingSlash(s3Endpoint);
        return endpoint + "/" + s3Bucket + "/" + key;
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

    private String trimTrailingSlash(final String value)
    {
        if (value == null)
        {
            return "";
        }

        String result = value.trim();
        while (result.endsWith("/"))
        {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public Collection<TokenBorderResponse> findAll()
    {
        return tokenBorderRepository.findAll(Sort.by(Sort.Direction.ASC, "order"))
                .stream()
                .map(tokenBorderMapper::toResponse)
                .toList();
    }
}
