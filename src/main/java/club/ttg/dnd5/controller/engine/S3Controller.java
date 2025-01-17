package club.ttg.dnd5.controller.engine;

import club.ttg.dnd5.dto.s3.S3UploadedFile;
import club.ttg.dnd5.service.engine.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@Tag(name = "S3", description = "Методы взаимодействия с S3")
@RequiredArgsConstructor
@RequestMapping("/api/s3")
@Secured("ADMIN")
public class S3Controller {
    private final S3Service s3Service;

    @Operation(summary = "Загрузка файла в S3")
    @PutMapping("/upload")
    public S3UploadedFile upload(
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String path
    ) throws IOException {
        return s3Service.upload(file, path);
    }

    @Operation(summary = "Удаление файла из S3")
    @DeleteMapping
    public void delete(@Valid @RequestParam String s3url) {
        s3Service.delete(s3url);
    }
}
