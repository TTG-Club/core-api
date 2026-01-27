package club.ttg.dnd5.domain.image.service;

import club.ttg.dnd5.security.SecurityUtils;
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
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ImageService {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String s3Bucket;

    @Value("${spring.cloud.aws.s3.endpoint}")
    private String s3Endpoint;

    @Value("${image.validation.max-bytes:1048576}")
    private long maxBytes;

    @Value("${image.validation.max-width:2048}")
    private int maxWidth;

    @Value("${image.validation.max-height:2048}")
    private int maxHeight;

    @Value("${image.validation.allowed-types:png,jpg,jpeg,webp}")
    private List<String> allowedContentTypes;

    private final S3Client s3Client;

    @Secured("ADMIN")
    public String upload(final String prefix, final MultipartFile file) {
        validateUpload(file);

        String key = buildKey(prefix, file);

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

        return buildPublicUrl(key);
    }

    @Secured("ADMIN")
    public void delete(final String url) {
        String key = extractKeyFromUrl(url);

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
        validateContentType(file);
        validateDimensions(file);
    }

    private void validateBytes(final MultipartFile file)
    {
        final long size = file.getSize();

        if (size <= 0)
        {
            throw new ImageValidationException("File size is 0 bytes");
        }

        if (size > maxBytes)
        {
            throw new ImageValidationException(
                    "File is too large: " + size + " bytes (max " + maxBytes + ")"
            );
        }
    }

    private void validateContentType(final MultipartFile file)
    {
        final String contentType = file.getContentType();

        if (contentType == null || !allowedContentTypes.contains(contentType))
        {
            throw new ImageValidationException("Unsupported content type: " + contentType);
        }
    }

    private void validateDimensions(final MultipartFile file)
    {
        final ImageDimensions dims = readDimensions(file);

        if (dims.width() <= 0 || dims.height() <= 0)
        {
            throw new ImageValidationException(
                    "Invalid image dimensions: " + dims.width() + "x" + dims.height()
            );
        }

        if (dims.width() > maxWidth || dims.height() > maxHeight)
        {
            throw new ImageValidationException(
                    "Image is too large: " + dims.width() + "x" + dims.height()
                            + " (max " + maxWidth + "x" + maxHeight + ")"
            );
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

        String extension = extractExtension(file.getOriginalFilename());
        if (extension.isBlank())
        {
            extension = "webp";
        }

        return normalizedPrefix
                + "/" + username
                + "/" + UUID.randomUUID()
                + "-" + originalName
                + "." + extension;
    }

    private String buildPublicUrl(final String key)
    {
        String endpoint = trimTrailingSlash(s3Endpoint);
        return endpoint + "/" + s3Bucket + "/" + key;
    }

    private String normalizePrefix(final String prefix)
    {
        if (prefix == null || prefix.isBlank())
        {
            throw new IllegalArgumentException("Prefix must not be empty");
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

    private String extractKeyFromUrl(final String url)
    {
        if (url == null || url.isBlank())
        {
            throw new IllegalArgumentException("Image url is empty");
        }

        URI uri = URI.create(url);
        String path = uri.getPath(); // "/<bucket>/<key>"

        String expectedPrefix = "/" + s3Bucket + "/";
        if (!path.startsWith(expectedPrefix))
        {
            throw new IllegalArgumentException("Url does not match bucket '" + s3Bucket + "': " + url);
        }

        String key = path.substring(expectedPrefix.length());
        if (key.isBlank())
        {
            throw new IllegalArgumentException("Could not extract S3 key from url: " + url);
        }

        return key;
    }

    private ImageDimensions readDimensions(final MultipartFile file)
    {
        try (InputStream in = file.getInputStream();
             ImageInputStream iis = ImageIO.createImageInputStream(in))
        {
            if (iis == null)
            {
                throw new ImageValidationException("Unable to open image stream");
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
            throw new ImageValidationException("Failed to read image dimensions", e);
        }
    }

    private ImageDimensions readDimensionsByDecoding(final MultipartFile file)
    {
        try (InputStream in = file.getInputStream())
        {
            final BufferedImage img = ImageIO.read(in);
            if (img == null)
            {
                throw new ImageValidationException("File is not a valid image");
            }

            return new ImageDimensions(img.getWidth(), img.getHeight());
        }
        catch (IOException e)
        {
            throw new ImageValidationException("Failed to decode image", e);
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
