package club.ttg.dnd5.controller;

import club.ttg.dnd5.dto.MenuApi;
import club.ttg.dnd5.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Tag(name = "Меню сайта", description = "The Menu API")
@RestController
@RequestMapping("/api/v2/menu")
public class MenuApiController {
    private final MenuService menuService;

    @Operation(summary = "Получение списка элементов меню")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MenuApi> getAllMenus() {
        return menuService.getAllMenus();
    }

    @Operation(summary = "Получение элемента меню по ID")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MenuApi getMenuById(@PathVariable Long id) {
        return menuService.getMenuById(id);
    }

    @Operation(summary = "Создание нового элемента меню")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuApi createMenu(@RequestBody MenuApi menuApi) {
        return menuService.createMenu(menuApi);
    }

    @Operation(summary = "Обновление элемента меню по ID")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MenuApi updateMenu(@PathVariable Long id, @RequestBody MenuApi menuApi) {
        return menuService.updateMenu(id, menuApi);
    }

    @Operation(summary = "Удаление элемента меню по ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
    }
}