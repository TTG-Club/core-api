package club.ttg.dnd5.controller.item;

import club.ttg.dnd5.dto.item.ItemDto;
import club.ttg.dnd5.service.item.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/item")
@Tag(name = "Снаряжение и предметы", description = "REST API снаряжение и прочие предметы")
public class ItemController {
    private final ItemService itemService;

    @Operation(summary = "Получение детального описания предмета")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предмет успешно получена"),
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
            @ApiResponse(responseCode = "200", description = "Предмет удалена из общего списка"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("{itemUrl}")
    public ItemDto deleteItem(@PathVariable final String itemUrl) {
        return itemService.delete(itemUrl);
    }
}
