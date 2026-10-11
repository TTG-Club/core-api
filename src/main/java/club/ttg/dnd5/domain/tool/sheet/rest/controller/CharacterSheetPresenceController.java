package club.ttg.dnd5.domain.tool.sheet.rest.controller;

import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetPresenceResponse;
import club.ttg.dnd5.domain.tool.sheet.service.CharacterSheetPresenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Мягкая блокировка листа: отметка «лист открыт у меня» и список тех, у кого он открыт ещё.
 * Отмечаться могут владелец и редакторы с выданным правом. Закрыт ролью, как и остальной инструмент.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/tools/character-sheet")
@Secured("USER")
@Tag(name = "Присутствие в листе персонажа",
        description = "REST API мягкой блокировки: кто сейчас держит лист открытым на правку")
public class CharacterSheetPresenceController {

    private final CharacterSheetPresenceService presenceService;

    @Operation(summary = "Отметить, что лист открыт, и узнать, у кого ещё он открыт и какая версия "
            + "листа на сервере. Слать, пока лист открыт: отметка живёт 45 секунд")
    @PostMapping("/{id}/presence")
    public CharacterSheetPresenceResponse heartbeat(@PathVariable final UUID id) {
        return presenceService.heartbeat(id);
    }

    @Operation(summary = "Снять свою отметку — лист закрыт")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}/presence")
    public void leave(@PathVariable final UUID id) {
        presenceService.leave(id);
    }
}
