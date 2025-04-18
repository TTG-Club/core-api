package club.ttg.dnd5.domain.magic.rest.controller;

import club.ttg.dnd5.domain.magic.rest.dto.MagicItemDetailResponse;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemRequest;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemShortResponse;
import club.ttg.dnd5.domain.magic.service.MagicItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/magic-item")
@Tag(name = "Магически предметы", description = "REST API магические предметы и артефакты")
public class ItemMagicController {
    private final MagicItemService magicItemService;
    /**
     * Проверка существования вида по URL.
     *
     * @param url URL вида.
     * @return 204, если вида с таким URL не существует; 409, если вид существует.
     */
    @Operation(
            summary = "Проверка существования предмета",
            description = "Возвращает 204 (No Content), если предмет с указанным URL не существует, или 409 (Conflict), если существует."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Предмет с указанным URL не найден."),
            @ApiResponse(responseCode = "409", description = "Предмет с указанным URL уже существует.")
    })
    @RequestMapping(value = "/{url}", method = RequestMethod.HEAD)
    @ResponseStatus(HttpStatus.CONFLICT)
    public boolean exists(@PathVariable("url") String url) {
        return magicItemService.existsByUrl(url);
    }

    @Operation(summary = "Получение детального описания предмета")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предмет успешно получен"),
            @ApiResponse(responseCode = "404", description = "Предмет не найден")
    })
    @GetMapping("/{url}")
    public MagicItemDetailResponse getItem(@PathVariable final String url) {
        return magicItemService.getItem(url);
    }

    @Operation(summary = "Получение списка краткого описания предметов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предметы успешно получены")
    })
    @PostMapping("/search")
    public Collection<MagicItemShortResponse> getItems(@RequestParam(name = "query", required = false)
                                                       @Valid
                                                       @Size(min = 3)
                                                       @Schema( description = "Строка поиска, если null-отдаются все сущности")
                                                       String searchLine) {
        return magicItemService.getItems(searchLine);
    }

    @Secured("ADMIN")
    @Operation(summary = "Добавление предмета")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Предмет успешно добавлен"),
            @ApiResponse(responseCode = "400", description = "Предмет уже существует"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public String addItem(@RequestBody final MagicItemRequest itemDto) {
        return magicItemService.addItem(itemDto);
    }

    @Secured("ADMIN")
    @Operation(summary = "Обновление предмета")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Предмет успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Предмет не существует"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PutMapping("{url}")
    public String updateItem(@PathVariable final String url,
                                         @RequestBody final MagicItemRequest itemDto) {
        return magicItemService.updateItem(url, itemDto);
    }

    @Secured("ADMIN")
    @Operation(summary = "Скрывает предмет")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предмет удален из общего списка"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @DeleteMapping("{itemUrl}")
    public String deleteItem(@PathVariable final String itemUrl) {
        return magicItemService.delete(itemUrl);
    }
}
