package club.ttg.dnd5.domain.charlist.rest.controller;

import club.ttg.dnd5.domain.charlist.rest.dto.CharlistRequest;
import club.ttg.dnd5.domain.charlist.rest.dto.CharlistResponse;
import club.ttg.dnd5.domain.charlist.rest.dto.CharlistShortResponse;
import club.ttg.dnd5.domain.charlist.service.CharlistService;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Чарлисты", description = "REST API для управления чарлистами персонажей")
@RestController
@RequestMapping("/api/v2/charlists")
@RequiredArgsConstructor
public class CharlistController {
    private final CharlistService charlistService;

    @Operation(summary = "Получить все чарлисты текущего пользователя")
    @GetMapping
    public List<CharlistShortResponse> getMyCharlists() {
        User user = SecurityUtils.getUser();
        return charlistService.getMyCharlists(user.getUuid());
    }

    @Operation(summary = "Получить чарлист по ID")
    @GetMapping("/{id}")
    public CharlistResponse getCharlistById(@PathVariable UUID id) {
        User user = SecurityUtils.getUser();
        return charlistService.getCharlistById(id, user.getUuid());
    }

    @Operation(summary = "Получить чарлист по share-токену (доступ по ссылке)")
    @GetMapping("/shared/{shareToken}")
    public CharlistResponse getCharlistByShareToken(@PathVariable String shareToken) {
        return charlistService.getCharlistByShareToken(shareToken);
    }

    @Operation(summary = "Получить все публичные чарлисты")
    @GetMapping("/public")
    public List<CharlistShortResponse> getPublicCharlists() {
        return charlistService.getPublicCharlists();
    }

    @Operation(summary = "Создать новый чарлист")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CharlistResponse createCharlist(@Valid @RequestBody CharlistRequest request) {
        User user = SecurityUtils.getUser();
        return charlistService.createCharlist(request, user.getUuid());
    }

    @Operation(summary = "Обновить чарлист")
    @PutMapping("/{id}")
    public CharlistResponse updateCharlist(@PathVariable UUID id,
                                           @Valid @RequestBody CharlistRequest request) {
        User user = SecurityUtils.getUser();
        return charlistService.updateCharlist(id, request, user.getUuid());
    }

    @Operation(summary = "Удалить чарлист")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCharlist(@PathVariable UUID id) {
        User user = SecurityUtils.getUser();
        charlistService.deleteCharlist(id, user.getUuid());
    }
}
