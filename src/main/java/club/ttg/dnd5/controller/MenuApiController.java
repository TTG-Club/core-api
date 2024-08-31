package club.ttg.dnd5.controller;

import club.ttg.dnd5.dto.engine.MenuResponse;
import club.ttg.dnd5.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
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
    public List<MenuResponse> getAllMenus() {
        return menuService.findAll();
    }

    @Operation(summary = "Получение элемента меню по URL")
    @GetMapping("/{url}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MenuResponse> getMenuByUrl(@PathVariable String url) {
        return menuService.findByUrl(url)
                .map(menuApi -> new ResponseEntity<>(menuApi, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Создание нового элемента меню")
    @PostMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuResponse createMenu(@RequestBody MenuResponse menuResponse) {
        return menuService.save(menuResponse);
    }

    @Operation(summary = "Обновление элемента меню по URL")
    @PutMapping("/{url}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MenuResponse> updateMenu(@PathVariable String url, @RequestBody MenuResponse menuResponse) {
        menuResponse.setUrl(url);
        MenuResponse updatedMenu = menuService.update(menuResponse);
        return new ResponseEntity<>(updatedMenu, HttpStatus.OK);
    }

    @Operation(summary = "Удаление элемента меню по URL")
    @DeleteMapping("/{url}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenu(@PathVariable String url) {
        menuService.deleteByUrl(url);
    }
}