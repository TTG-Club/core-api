package club.ttg.dnd5.controller;

import club.ttg.dnd5.dto.engine.MenuApi;
import club.ttg.dnd5.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        return menuService.findAll();
    }

    @Operation(summary = "Получение элемента меню по URL")
    @GetMapping("/{url}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MenuApi> getMenuByUrl(@PathVariable String url) {
        return menuService.findByUrl(url)
                .map(menuApi -> new ResponseEntity<>(menuApi, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Создание нового элемента меню")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuApi createMenu(@RequestBody MenuApi menuApi) {
        return menuService.save(menuApi);
    }

    @Operation(summary = "Обновление элемента меню по URL")
    @PutMapping("/{url}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MenuApi> updateMenu(@PathVariable String url, @RequestBody MenuApi menuApi) {
        menuApi.setUrl(url);
        try {
            MenuApi updatedMenu = menuService.update(menuApi);
            return new ResponseEntity<>(updatedMenu, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Удаление элемента меню по URL")
    @DeleteMapping("/{url}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenu(@PathVariable String url) {
        menuService.deleteByUrl(url);
    }
}