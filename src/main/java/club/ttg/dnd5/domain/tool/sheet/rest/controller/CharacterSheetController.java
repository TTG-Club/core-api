package club.ttg.dnd5.domain.tool.sheet.rest.controller;

import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetListResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetRequest;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetResponse;
import club.ttg.dnd5.domain.tool.sheet.service.CharacterSheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Доступ: лист виден и редактируем только владельцу (uuid из JWT). Пока фича закрыта —
 * {@code @Secured("ADMIN")} на классе (снять при открытии всем): {@code /api/v2/**} на уровне
 * фильтров — permitAll, авторизацию обеспечивают только аннотации методов.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/tools/character-sheet")
@Secured("ADMIN")
@Tag(name = "Лист персонажа",
        description = "REST API листов персонажей: сохранение листа одним JSON-документом, "
                + "список с лимитом, мягкое удаление и восстановление")
public class CharacterSheetController {

    private final CharacterSheetService sheetService;

    @Operation(summary = "Создание листа: до 2 активных на пользователя (лимит вернёт 400); "
            + "data — лист целиком, обязателен")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CharacterSheetResponse create(@RequestBody @Valid final CharacterSheetRequest request) {
        return sheetService.create(request);
    }

    @Operation(summary = "Листы текущего пользователя с лимитом и числом активных; "
            + "includeDeleted=true — вместе с историей удалённых (у них data = null)")
    @GetMapping
    public CharacterSheetListResponse findMine(
            @RequestParam(required = false, defaultValue = "false")
            @Schema(description = "Включить удалённые листы (история с восстановлением)") final boolean includeDeleted) {
        return sheetService.findMine(includeDeleted);
    }

    @Operation(summary = "Лист целиком по идентификатору")
    @GetMapping("/{id}")
    public CharacterSheetResponse findById(@PathVariable final UUID id) {
        return sheetService.findById(id);
    }

    @Operation(summary = "Обновление листа: название и/или документ. Применяются только переданные поля")
    @PutMapping("/{id}")
    public CharacterSheetResponse update(@PathVariable final UUID id,
                                         @RequestBody @Valid final CharacterSheetRequest request) {
        return sheetService.update(id, request);
    }

    @Operation(summary = "Мягкое удаление: лист уходит в историю (хранятся последние 10 удалённых) "
            + "и может быть восстановлен")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable final UUID id) {
        sheetService.delete(id);
    }

    @Operation(summary = "Восстановление удалённого листа; при заполненном лимите активных — 400")
    @PostMapping("/{id}/restore")
    public CharacterSheetResponse restore(@PathVariable final UUID id) {
        return sheetService.restore(id);
    }
}
