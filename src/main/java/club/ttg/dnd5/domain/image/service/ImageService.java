package club.ttg.dnd5.domain.image.service;

import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.security.SecurityUtils;
import club.ttg.dnd5.domain.image.service.ImageConverter.ConvertedImage;
import club.ttg.dnd5.domain.image.service.ImageConverter.WebpOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ImageService
{
    @Value("${spring.cloud.aws.s3.bucket}")
    private String s3Bucket;

     @Value("${image.validation.max-bytes:1048576}")
    private long maxBytes;

    @Value("${image.validation.max-width:2048}")
    private int maxWidth;

    @Value("${image.validation.max-height:2048}")
    private int maxHeight;

    @Value("${image.validation.allowed-types:png,jpg,jpeg,webp,bmp}")
    private List<String> allowedTypes;

    @Value("${image.webp.quality:0.82}")
    private float webpQuality;

    @Value("${image.webp.lossless:false}")
    private boolean webpLossless;

    @Value("${image.webp.preserve-alpha:true}")
    private boolean webpPreserveAlpha;

    private final S3Client s3Client;

    @PostConstruct
    public void init() {
        ImageIO.scanForPlugins();
    }

    @Secured("ADMIN")
    public String upload(final SectionType prefix, final MultipartFile file)
    {
        validateUpload(file);

        final ConvertedImage converted = convertToWebp(file);

        final String key = buildKey(prefix.getValue(), file);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Bucket)
                .key(key)
                .contentType("image/webp")
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(converted.bytes()));

        return "/s3/" + key;
    }

    /**
     * Конвертирует изображение в WebP без сохранения в S3.
     * Возвращает готовые байты WebP.
     */
    public byte[] convert(final MultipartFile file)
    {
        validateUpload(file);

        final ConvertedImage converted = convertToWebp(file);

        if (converted.bytes() == null || converted.bytes().length == 0)
        {
            throw new ImageValidationException("Не удалось сконвертировать изображение в WebP");
        }

        return converted.bytes();
    }

    @Secured("ADMIN")
    public void delete(final String key)
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
            // best-effort cleanup
        }
    }

    public void validateUpload(final MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("Файл пустой");
        }

        validateBytes(file);
        validateType(file);
        validateDimensions(file);
    }

    private void validateBytes(final MultipartFile file)
    {
        final long size = file.getSize();

        if (size <= 0)
        {
            throw new ImageValidationException("Размер изображения 0 байт");
        }

        if (size > maxBytes)
        {
            throw new ImageValidationException(
                    "Размер изображения выше допустимого: " + size + " байт (максимально " + maxBytes + " байт)"
            );
        }
    }

    private void validateType(final MultipartFile file)
    {
        final String originalFilename = file.getOriginalFilename();
        final String ext = extractExtension(originalFilename);

        if (!ext.isBlank() && allowedTypes.contains(ext.toLowerCase()))
        {
            return;
        }

        final ImageConverter.SourceFormat detected = detectFormat(file);
        final String detectedExt = switch (detected)
        {
            case JPEG -> "jpg";
            case PNG -> "png";
            case BMP -> "bmp";
            case WEBP -> "webp";
            default -> "";
        };

        if (detectedExt.isBlank() || !allowedTypes.contains(detectedExt))
        {
            throw new ImageValidationException("Не поддерживаемый тип изображения: " + detected);
        }
    }

    private ImageConverter.SourceFormat detectFormat(final MultipartFile file)
    {
        try (InputStream in = file.getInputStream())
        {
            byte[] head = in.readNBytes(32);
            return ImageConverter.SourceFormat.fromSignature(head)
                    .orElse(ImageConverter.SourceFormat.UNKNOWN);
        }
        catch (IOException e)
        {
            throw new ImageValidationException("Ошибка чтения заголовка изображения", e);
        }
    }

    private void validateDimensions(final MultipartFile file)
    {
        final ImageDimensions dims = readDimensions(file);

        if (dims.width() <= 0 || dims.height() <= 0)
        {
            throw new ImageValidationException(
                    "Неверный размер изображения: " + dims.width() + "x" + dims.height()
            );
        }

        if (dims.width() > maxWidth || dims.height() > maxHeight)
        {
            throw new ImageValidationException(
                    "Изображение велико: " + dims.width() + "x" + dims.height()
                            + " (max " + maxWidth + "x" + maxHeight + ")"
            );
        }
    }

    private ConvertedImage convertToWebp(final MultipartFile file)
    {
        final byte[] bytes;
        try
        {
            bytes = file.getBytes();
        }
        catch (IOException exception)
        {
            throw new IllegalStateException("Ошибка чтения изображения", exception);
        }

        WebpOptions options;
        if (webpLossless)
        {
            options = WebpOptions.lossless();
        }
        else
        {
            options = WebpOptions.lossy(webpQuality);
        }

        if (!webpPreserveAlpha)
        {
            options = options.withoutAlpha();
        }

        try
        {
            return ImageConverter.toWebp(bytes, file.getContentType(), file.getOriginalFilename(), options);
        }
        catch (IOException e)
        {
            throw new ImageValidationException("Failed to convert image to WebP", e);
        }
    }

    private String buildKey(final String prefix, final MultipartFile file)
    {
        String normalizedPrefix = normalizePrefix(prefix);
        String username = SecurityUtils.getUser().getUsername();

        String originalName = extractBaseName(file.getOriginalFilename());
        if (originalName.isBlank())
        {
            originalName = "file";
        }

        return normalizedPrefix
                + "/" + username
                + "/" + UUID.randomUUID()
                + "-" + originalName
                + ".webp";
    }

    private String normalizePrefix(final String prefix)
    {
        if (prefix == null || prefix.isBlank())
        {
            throw new IllegalArgumentException("Префикс должен быть задан");
        }

        String result = prefix.trim();
        while (result.startsWith("/"))
        {
            result = result.substring(1);
        }
        while (result.endsWith("/"))
        {
            result = result.substring(0, result.length() - 1);
        }
        return result;
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

    private String extractBaseName(final String filename)
    {
        if (filename == null)
        {
            return "";
        }

        String name = filename;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0)
        {
            name = name.substring(slash + 1);
        }

        int dot = name.lastIndexOf('.');
        if (dot > 0)
        {
            name = name.substring(0, dot);
        }

        return name
                .trim()
                .replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    private ImageDimensions readDimensions(final MultipartFile file)
    {
        try (InputStream in = file.getInputStream();
             ImageInputStream iis = ImageIO.createImageInputStream(in))
        {
            if (iis == null)
            {
                throw new ImageValidationException("Не доступен стрим изображения");
            }

            final Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext())
            {
                return readDimensionsByDecoding(file);
            }

            final ImageReader reader = readers.next();
            try
            {
                reader.setInput(iis, true, true);
                return new ImageDimensions(
                        reader.getWidth(0),
                        reader.getHeight(0)
                );
            }
            finally
            {
                reader.dispose();
            }
        }
        catch (IOException e)
        {
            throw new ImageValidationException("Ошибка чтения размеров изображения", e);
        }
    }

    private ImageDimensions readDimensionsByDecoding(final MultipartFile file)
    {
        try (InputStream in = file.getInputStream())
        {
            final BufferedImage img = ImageIO.read(in);
            if (img == null)
            {
                throw new ImageValidationException("Файл не является изображением");
            }

            return new ImageDimensions(img.getWidth(), img.getHeight());
        }
        catch (IOException e)
        {
            throw new ImageValidationException("Ошибка декодирования изображения", e);
        }
    }

    public record ImageDimensions(int width, int height)
    {
    }

    public static class ImageValidationException extends RuntimeException
    {
        public ImageValidationException(final String message)
        {
            super(message);
        }

        public ImageValidationException(final String message, final Throwable cause)
        {
            super(message, cause);
        }
    }
}
