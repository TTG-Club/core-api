package club.ttg.dnd5.controller.engine;

import club.ttg.dnd5.dto.engine.MenuResponse;
import club.ttg.dnd5.service.engine.MenuService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Hidden
@Tag(name = "Меню сайта")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/menu")
public class MenuController {
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
        return new ResponseEntity<>(menuService.findByUrl(url), HttpStatus.OK);
    }

    @Operation(summary = "Создание нового элемента меню")
    @PostMapping
    @Secured("ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuResponse createMenu(@RequestBody MenuResponse menuResponse) {
        return menuService.save(menuResponse);
    }

    @Operation(summary = "Обновление элемента меню. NOTE теле должен быть новый url")
    @PutMapping("/{oldUrl}")
    @Secured("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MenuResponse> updateMenu(@PathVariable String oldUrl,
                                                   @RequestBody MenuResponse menuResponse) {
        MenuResponse updatedMenu = menuService.update(oldUrl, menuResponse);
        return new ResponseEntity<>(updatedMenu, HttpStatus.OK);
    }

    @Operation(summary = "Удаление элемента меню по URL")
    @DeleteMapping("/{url}")
    @Secured("ADMIN")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenu(@PathVariable String url) {
        menuService.deleteByUrl(url);
    }
}
