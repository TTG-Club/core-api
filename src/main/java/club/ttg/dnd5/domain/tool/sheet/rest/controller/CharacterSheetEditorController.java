package club.ttg.dnd5.domain.tool.sheet.rest.controller;

import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetEditRequestCountResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetEditorListResponse;
import club.ttg.dnd5.domain.tool.sheet.service.CharacterSheetEditorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Права на редактирование листа со стороны владельца: запросы, решение по ним и счётчик для точки
 * у шлема. Сам запрос отправляет сохранивший лист — ручкой {@link CharacterSheetSavedController}.
 * Закрыт ролью, как и остальной инструмент.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/tools/character-sheet")
@Secured("USER")
@Tag(name = "Редакторы листа персонажа",
        description = "REST API прав на редактирование листа: запросы, разрешение, отказ и отзыв")
public class CharacterSheetEditorController {

    private final CharacterSheetEditorService editorService;

    @Operation(summary = "Неотвеченные запросы на активные листы текущего пользователя")
    @GetMapping("/edit-requests/count")
    public CharacterSheetEditRequestCountResponse countIncoming() {
        return editorService.countIncoming();
    }

    @Operation(summary = "Запросы и редакторы листа: только владельцу, остальным — 403")
    @GetMapping("/{id}/editors")
    public CharacterSheetEditorListResponse findEditors(@PathVariable final UUID id) {
        return editorService.findEditors(id);
    }

    @Operation(summary = "Разрешить редактирование: до 5 редакторов на лист (сверх — 409), "
            + "отклонённый запрос — 409")
    @PostMapping("/{id}/editors/{editorId}/approve")
    public CharacterSheetEditorListResponse approve(@PathVariable final UUID id,
                                                    @PathVariable final UUID editorId) {
        return editorService.approve(id, editorId);
    }

    @Operation(summary = "Отклонить запрос или отозвать право: попросить заново можно сразу")
    @DeleteMapping("/{id}/editors/{editorId}")
    public CharacterSheetEditorListResponse remove(@PathVariable final UUID id,
                                                   @PathVariable final UUID editorId) {
        return editorService.remove(id, editorId);
    }
}
