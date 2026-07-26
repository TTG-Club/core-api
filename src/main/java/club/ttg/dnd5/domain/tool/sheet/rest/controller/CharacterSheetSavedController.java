package club.ttg.dnd5.domain.tool.sheet.rest.controller;

import club.ttg.dnd5.domain.tool.sheet.rest.dto.SavedCharacterSheetListResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.SavedCharacterSheetRequest;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.SavedCharacterSheetResponse;
import club.ttg.dnd5.domain.tool.sheet.service.SavedCharacterSheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Чужие листы персонажей, сохранённые по ссылке. Как и {@link CharacterSheetController}, закрыт
 * ролью: сохранять ссылки может только тот, у кого есть доступ к самому инструменту. Вынесено в
 * отдельный контроллер, чтобы литерал {@code /saved} не спорил с {@code /{id}} соседнего.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/tools/character-sheet/saved")
@Secured("ADMIN")
@Tag(name = "Сохранённые листы персонажей",
        description = "REST API чужих листов, сохранённых по ссылке «поделиться»: только просмотр")
public class CharacterSheetSavedController {

    private final SavedCharacterSheetService savedSheetService;

    @Operation(summary = "Сохранённые ссылки текущего пользователя с лимитом; "
            + "у листов, к которым доступ закрыт, data = null и available = false")
    @GetMapping
    public SavedCharacterSheetListResponse findMine() {
        return savedSheetService.findMine();
    }

    @Operation(summary = "Сохранение чужого листа по токену ссылки: до 16 записей (лимит вернёт 400). "
            + "Повторное сохранение того же листа обновляет ссылку, свой лист — 400")
    @PostMapping
    public SavedCharacterSheetResponse save(@RequestBody @Valid final SavedCharacterSheetRequest request) {
        return savedSheetService.save(request.getShareToken());
    }

    @Operation(summary = "Удаление сохранённой ссылки")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable final UUID id) {
        savedSheetService.delete(id);
    }
}
