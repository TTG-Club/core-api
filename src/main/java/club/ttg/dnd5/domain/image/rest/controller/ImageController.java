package club.ttg.dnd5.domain.image.rest.controller;

import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.image.service.ImageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Изображения")
@RequiredArgsConstructor
@RequestMapping("/api/v2/image")
@RestController
public class ImageController {
    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(SectionType prefix, @RequestPart("file") MultipartFile file) {
        return imageService.upload(prefix, file);
    }

    @PostMapping(
            path = "/.webp",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = "image/webp"
    )
    public ResponseEntity<byte[]> convert(@RequestPart("file") MultipartFile file)
    {
        byte[] webp = imageService.convert(file);

        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf("image/webp"))
                .contentLength(webp.length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"converted.webp\""
                )
                .body(webp);
    }

    @DeleteMapping("/{url}")
    public void delete(@RequestParam String url) {
        imageService.delete(url);
    }
}
