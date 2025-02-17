package club.ttg.dnd5.controller.item;

import club.ttg.dnd5.dto.item.ItemDto;
import club.ttg.dnd5.service.item.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/item")
@Tag(name = "Снаряжение и предметы", description = "REST API снаряжение и прочие предметы")
public class ItemController {
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
            @ApiResponse(responseCode = "204", description = "Предмет с указанным URL не найден."),
            @ApiResponse(responseCode = "409", description = "Предмет с указанным URL уже существует.")
    })
    @RequestMapping(value = "/{url}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> handleOptions(@PathVariable("url") String url) {
        boolean exists = itemService.existsByUrl(url);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @Operation(summary = "Получение детального описания предмета")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предмет успешно получен"),
            @ApiResponse(responseCode = "404", description = "Предмет не найден")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{itemUtl}")
    public ItemDto getItem(@PathVariable final String itemUtl) {
        return itemService.getItem(itemUtl);
    }

    @Operation(summary = "Получение списка краткого описания предметов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предметы успешно получены")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/search")
    public Collection<ItemDto> getItems() {
        return itemService.getItems();
    }

    @Operation(summary = "Добавление предмета")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Предмет успешно добавлен"),
            @ApiResponse(responseCode = "400", description = "Предмет уже существует"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ItemDto addItem(@RequestBody final ItemDto itemDto) {
        return itemService.addItem(itemDto);
    }

    @Operation(summary = "Обновление предмета")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Предмет успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Предмет не существует"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("{itemUrl}")
    public ItemDto updateItem(@PathVariable final String itemUrl,
            @RequestBody final ItemDto itemDto) {
        return itemService.updateItem(itemUrl, itemDto);
    }

    @Operation(summary = "Скрывает предмет")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предмет удален из общего списка"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("{itemUrl}")
    public ItemDto deleteItem(@PathVariable final String itemUrl) {
        return itemService.delete(itemUrl);
    }
}
