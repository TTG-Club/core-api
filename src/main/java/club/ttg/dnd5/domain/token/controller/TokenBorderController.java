package club.ttg.dnd5.domain.token.controller;

import club.ttg.dnd5.domain.token.rest.dto.TokenBorderReorderRequest;
import club.ttg.dnd5.domain.token.rest.dto.TokenBorderResponse;
import club.ttg.dnd5.domain.token.service.TokenBorderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.UUID;

@Tag(name = "Рамки токенов")
@RequiredArgsConstructor
@RequestMapping("/api/v2/token-border")
@RestController
public class TokenBorderController {
    private final TokenBorderService tokenBorderService;

    @Operation(summary = "Возвращает все токены")
    @GetMapping
    public Collection<TokenBorderResponse> getBorders()
    {
        return tokenBorderService.findAll();
    }

    @Operation(summary = "Добавление токена", description = "Регистрирует токен и загружает его в S3")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TokenBorderResponse upload(@RequestPart("file") MultipartFile file) {
        return tokenBorderService.createAndUpload(file);
    }

    @Operation(summary = "Изменение порядка токенов")
    @PutMapping
    public void updateOrder(@Valid @RequestBody TokenBorderReorderRequest request){
        tokenBorderService.updateOrder(request);
    }

    @Operation(summary = "Удаление токена")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id)
    {
        tokenBorderService.delete(id);
    }
}
