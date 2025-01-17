package club.ttg.dnd5.service.engine;

import club.ttg.dnd5.dto.s3.S3UploadedFile;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.model.user.User;
import club.ttg.dnd5.security.SecurityUtils;
import club.ttg.dnd5.utills.SlugifyUtil;
import io.awspring.cloud.s3.Location;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3Service {
    @Value("${spring.cloud.aws.s3.endpoint}")
    private String ENDPOINT;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String BUCKET;

    private final S3Template s3Template;

    public S3UploadedFile upload(MultipartFile file, String path) throws IOException {
        String fileKey = getFileKey(file.getOriginalFilename(), path);

        S3Resource resource = s3Template.upload(
                BUCKET,
                fileKey,
                getInputStream(file),
                getObjectMetadata(file));

        return S3UploadedFile.builder()
                .filename(file.getOriginalFilename())
                .url(getResourceUrl(resource))
                .build();
    }

    public void delete(String s3url) {
        s3Template.deleteObject(s3url);
    }

    private String getFileKey(String filename, String path) {
        User user = SecurityUtils.getUser();

        String formattedPath = Arrays.stream(path
                .split("/"))
                .filter(folder -> !folder.isBlank())
                .map(SlugifyUtil::getSlug)
                .collect(Collectors.joining("/"));

        String key = String.format("%s/%s/%s", formattedPath, SlugifyUtil.getSlug(user.getUsername()), SlugifyUtil.getFilenameSlug(filename));

        if (key.getBytes().length > 1024) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Слишком длинное имя файла");
        }

        return key;
    }

    private ObjectMetadata getObjectMetadata(MultipartFile file) {
        return ObjectMetadata.builder()
                .contentType(file.getContentType())
                .cacheControl("public, must-revalidate, proxy-revalidate, max-age=31536000, s-maxage=31536000")
                .build();
    }

    private InputStream getInputStream(MultipartFile file) throws IOException {
        return new BufferedInputStream(file.getInputStream());
    }

    private String getResourceUrl(S3Resource resource) {
        Location location = resource.getLocation();

        return String.format("%s/%s/%s", ENDPOINT, location.getBucket(), location.getObject());
    }
}
