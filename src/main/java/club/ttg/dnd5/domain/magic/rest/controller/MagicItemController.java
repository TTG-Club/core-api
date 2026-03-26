package club.ttg.dnd5.domain.magic.rest.controller;


import club.ttg.dnd5.domain.magic.rest.dto.MagicItemDetailResponse;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemRequest;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemShortResponse;
import club.ttg.dnd5.domain.magic.service.MagicItemFilterService;
import club.ttg.dnd5.domain.magic.service.MagicItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/magic-items")
@Tag(name = "Магически предметы", description = "REST API магические предметы и артефакты")
public class MagicItemController {
    private final MagicItemFilterService magicItemFilterService;
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
            @ApiResponse(responseCode = "200", description = "Предмет с указанным URL уже существует."),
            @ApiResponse(responseCode = "404", description = "Предмет с указанным URL не найден."),
    })
    @RequestMapping(value = "/{url}", method = RequestMethod.HEAD)
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

    @GetMapping("/{url}/raw")
    public MagicItemRequest getMagicItemFormByUrl(@PathVariable String url) {
        return magicItemService.findFormByUrl(url);
    }



    @Operation(summary = "Поиск предметов v2", description = "Поиск магических предметов с Base64url-encoded фильтрами и пагинацией")
    @GetMapping("/search/v2")
    public Collection<MagicItemShortResponse> searchV2(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "f", required = false)
            @Schema(description = "Base64url-encoded JSON фильтров") String f,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size)
    {
        var request = club.ttg.dnd5.domain.filter.rest.SearchRequestResolver.resolve(
                f, search, page, size, club.ttg.dnd5.domain.magic.rest.dto.MagicItemSearchRequest.class);
        return magicItemService.searchV2(request);
    }



    @Operation(summary = "Получить метаданные фильтров v2", description = "Возвращает JSON для построения UI фильтров")
    @GetMapping("/filters/v2")
    public club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse getFiltersV2() {
        return magicItemFilterService.getFilterMetadata();
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

    @Operation(summary = "Предпросмотр предмета")
    @Secured("ADMIN")
    @PostMapping("/preview")
    public MagicItemDetailResponse preview(@RequestBody MagicItemRequest request) {
        return magicItemService.preview(request);
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
