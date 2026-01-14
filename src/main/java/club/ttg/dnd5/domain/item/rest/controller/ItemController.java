package club.ttg.dnd5.domain.item.rest.controller;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;
import club.ttg.dnd5.domain.item.service.ItemFilterService;
import club.ttg.dnd5.domain.item.service.ItemService;
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
@RequestMapping("/api/v2/item")
@Tag(name = "Снаряжение и предметы", description = "REST API снаряжение и прочие предметы")
public class ItemController {
    private final ItemFilterService itemFilterService;
    private final ItemService itemService;
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
            @ApiResponse(responseCode = "404", description = "Предмет с указанным URL не найден."),
            @ApiResponse(responseCode = "200", description = "Предмет с указанным URL уже существует.")
    })
    @RequestMapping(value = "/{url}", method = RequestMethod.HEAD)
    @ResponseStatus(HttpStatus.CONFLICT)
    public boolean exists(@PathVariable("url") String url) {
        return itemService.existOrThrow(url);
    }

    @Operation(summary = "Получение детального описания предмета")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предмет успешно получен"),
            @ApiResponse(responseCode = "404", description = "Предмет не найден")
    })
    @GetMapping("/{url}")
    public ItemDetailResponse getItem(@PathVariable final String url) {
        return itemService.getItem(url);
    }

    @GetMapping("/{url}/raw")
    public ItemRequest getSpellFormByUrl(@PathVariable String url) {
        return itemService.findFormByUrl(url);
    }

    @Operation(summary = "Список предметов", description = "Список предметов, поиск и фильтрация")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предметы успешно получены")
    })
    @GetMapping
    public Collection<ItemShortResponse> getItems(@RequestParam(name = "search", required = false)
                                                  @Valid
                                                  @Size(min = 2)
                                                  @Schema( description = "Строка поиска, если null-отдаются все сущности")
                                                  String searchLine,
                                                  @Schema(description = "упакованный в строку json фильтров")
                                                  @RequestParam(required = false) String filter) {
        return itemService.getItems(searchLine, filter);
    }


    @Operation(summary = "Получение списка краткого описания предметов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предметы успешно получены")
    })
    @PostMapping("/search")
    public Collection<ItemShortResponse> getItems(@RequestParam(name = "query", required = false)
                                                  @Valid
                                                  @Size(min = 2)
                                                  @Schema( description = "Строка поиска, если null-отдаются все сущности")
                                                  String searchLine,
                                                  @RequestBody(required = false) SearchBody searchBody) {
        return itemService.getItems(searchLine, searchBody);
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
    public String addItem(@RequestBody final ItemRequest itemDto) {
        return itemService.addItem(itemDto);
    }

    @GetMapping("/filters")
    public FilterInfo getFilters() {
        return itemFilterService.getDefaultFilterInfo();
    }

    @Operation(summary = "Предпросмотр предмета")
    @Secured("ADMIN")
    @PostMapping("/preview")
    public ItemDetailResponse preview(@RequestBody ItemRequest request) {
        return itemService.preview(request);
    }

    @Secured("ADMIN")
    @Operation(summary = "Обновление предмета")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Предмет успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Предмет не существует"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PutMapping("{itemUrl}")
    public String updateItem(@PathVariable final String itemUrl,
                                         @RequestBody final ItemRequest itemDto) {
        return itemService.updateItem(itemUrl, itemDto);
    }

    @Secured("ADMIN")
    @Operation(summary = "Скрывает предмет")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предмет удален из общего списка"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @DeleteMapping("{itemUrl}")
    public String deleteItem(@PathVariable final String itemUrl) {
        return itemService.delete(itemUrl);
    }
}
