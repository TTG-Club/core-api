package club.ttg.dnd5.domain.magic.rest.controller;

import club.ttg.dnd5.domain.common.rest.dto.PageResponse;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Получение списка краткого описания предметов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предметы успешно получены")
    })
    @PostMapping("/search")
    public PageResponse<MagicItemShortResponse> getItems(
        @RequestParam(name = "query", required = false)
        @Valid
        @Size(min = 2)
        @Schema( description = "Строка поиска, если null-отдаются все сущности")
        String searchLine,
        @RequestParam(required = false, defaultValue = "1")
        int page,
        @RequestParam(required = false, defaultValue = "120")
        int limit,
        @RequestParam(required = false, defaultValue = "name")
        String[] sort,
        @RequestBody(required = false) SearchBody searchBody
    ) {
        return magicItemService.getItems(searchLine, page, limit, sort, searchBody);
    }

    @GetMapping("/filters")
    public FilterInfo getFilters() {
        return magicItemFilterService.getDefaultFilterInfo();
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
