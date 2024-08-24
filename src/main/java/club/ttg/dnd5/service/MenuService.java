package club.ttg.dnd5.service;

import club.ttg.dnd5.dto.engine.MenuApi;
import club.ttg.dnd5.model.engine.Menu;
import club.ttg.dnd5.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;

    public List<MenuApi> getAllMenus() {
        return menuRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public MenuApi getMenuById(Long id) {
        return menuRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("Menu not found"));
    }

    public MenuApi createMenu(MenuApi menuApi) {
        Menu menu = convertToEntity(menuApi);
        Menu savedMenu = menuRepository.save(menu);
        return convertToDto(savedMenu);
    }

    public MenuApi updateMenu(Long id, MenuApi menuApi) {
        Menu existingMenu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found"));

        existingMenu.setName(menuApi.getName());
        existingMenu.setIcon(menuApi.getIcon());
        existingMenu.setUrl(menuApi.getUrl());
        existingMenu.setOnlyDev(menuApi.getOnlyDev());
        existingMenu.setOrder(menuApi.getOrder());
        existingMenu.setOnIndex(menuApi.getOnIndex());
        existingMenu.setIndexOrder(menuApi.getIndexOrder());

        Menu updatedMenu = menuRepository.save(existingMenu);
        return convertToDto(updatedMenu);
    }

    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
    }

    private MenuApi convertToDto(Menu menu) {
        return MenuApi.builder()
                .name(menu.getName())
                .icon(menu.getIcon())
                .url(menu.getUrl())
                .onlyDev(menu.isOnlyDev())
                .children(menu.getChildren().stream().map(this::convertToDto).collect(Collectors.toList()))
                .order(menu.getOrder())
                .onIndex(menu.isOnIndex())
                .indexOrder(menu.getIndexOrder())
                .build();
    }

    private Menu convertToEntity(MenuApi menuApi) {
        return Menu.builder()
                .name(menuApi.getName())
                .icon(menuApi.getIcon())
                .url(menuApi.getUrl())
                .onlyDev(menuApi.getOnlyDev())
                .order(menuApi.getOrder())
                .onIndex(menuApi.getOnIndex())
                .indexOrder(menuApi.getIndexOrder())
                .build();
    }
}
