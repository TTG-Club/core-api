package club.ttg.dnd5.domain.tool.sheet.rest.controller;

import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetPublicResponse;
import club.ttg.dnd5.domain.tool.sheet.service.CharacterSheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Просмотр листа персонажа по ссылке «поделиться»: без авторизации и без роли.
 * <p>
 * Вынесено в отдельный контроллер, потому что {@link CharacterSheetController} закрыт
 * {@code @Secured("USER")} на уровне класса — аннотация распространилась бы и на эту ручку,
 * а ссылка должна открываться кем угодно, включая анонима. Ручек записи по токену здесь нет:
 * режим «только просмотр» обеспечен их отсутствием на сервере, а не блокировками на клиенте.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/tools/character-sheet/shared")
@Tag(name = "Лист персонажа по ссылке",
        description = "Публичное чтение листа персонажа по токену ссылки «поделиться»")
public class CharacterSheetShareController {

    private final CharacterSheetService sheetService;

    @Operation(summary = "Лист по ссылке: только чтение, без авторизации. "
            + "Неизвестный, отозванный или битый токен — 404")
    @GetMapping("/{token}")
    public CharacterSheetPublicResponse findShared(@PathVariable final String token) {
        return sheetService.findShared(token);
    }
}
