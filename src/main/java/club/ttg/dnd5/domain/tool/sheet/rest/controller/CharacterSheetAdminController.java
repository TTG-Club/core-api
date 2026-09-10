package club.ttg.dnd5.domain.tool.sheet.rest.controller;

import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetPublicResponse;
import club.ttg.dnd5.domain.tool.sheet.service.CharacterSheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Просмотр любого листа персонажа администратором — например, по ссылке из баг-репорта, даже если
 * владелец не делился листом.
 * <p>
 * Вынесено в отдельный контроллер, потому что {@link CharacterSheetController} закрыт
 * {@code @Secured("USER")} на уровне класса и работает только с листами владельца. Ручек записи
 * здесь нет: режим «только просмотр» обеспечен их отсутствием на сервере, а не блокировками
 * на клиенте.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/tools/character-sheet/admin")
@Secured("ADMIN")
@Tag(name = "Лист персонажа: администратор",
        description = "Чтение любого листа персонажа администратором, только просмотр")
public class CharacterSheetAdminController {

    private final CharacterSheetService sheetService;

    @Operation(summary = "Любой активный лист по идентификатору: только чтение, без токена ссылки. "
            + "Нет листа или он удалён — 404")
    @GetMapping("/{id}")
    public CharacterSheetPublicResponse findById(@PathVariable final UUID id) {
        return sheetService.findForAdmin(id);
    }
}
