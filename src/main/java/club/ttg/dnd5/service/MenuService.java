package club.ttg.dnd5.service;

import club.ttg.dnd5.dto.engine.MenuResponse;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.mapper.engine.MenuMapper;
import club.ttg.dnd5.model.engine.Menu;
import club.ttg.dnd5.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private static final MenuMapper menuMapper = MenuMapper.INSTANCE;

    public List<MenuResponse> findAll() {
        return menuRepository.findAll().stream()
                .map(menuMapper::menuToMenuApi)
                .toList();
    }

    public MenuResponse findByUrl(String url) {
        return menuRepository.findByUrl(url)
                .map(menuMapper::menuToMenuApi)
                .orElseThrow(EntityNotFoundException::new);
    }

    public MenuResponse save(MenuResponse menuResponse) {
        Menu menu = menuMapper.menuApiToMenu(menuResponse);
        Menu savedMenu = menuRepository.save(menu);
        return menuMapper.menuToMenuApi(savedMenu);
    }

    public MenuResponse update(String oldUrl, MenuResponse menuResponse) {
        if (menuRepository.existsByUrl(oldUrl)) {
            Menu existingMenu = menuRepository.findByUrl(oldUrl)
                    .orElseThrow(() -> new EntityNotFoundException("Menu with URL " + oldUrl + " does not exist."));

            updateExistingMenu(menuResponse, existingMenu);

            if (menuResponse.getChildren() != null && !menuResponse.getChildren().isEmpty()) {
                List<Menu> updatedChildren = menuResponse.getChildren().stream()
                        .map(menuMapper::menuApiToMenu)
                        .toList();
                existingMenu.setChildren(updatedChildren);
            }

            Menu updatedMenu = menuRepository.save(existingMenu);
            return menuMapper.menuToMenuApi(updatedMenu);
        } else {
            throw new EntityNotFoundException("Menu with URL " + oldUrl + " does not exist.");
        }
    }

    public void deleteByUrl(String url) {
        if (menuRepository.existsByUrl(url)) {
            menuRepository.deleteByUrl(url);
        } else {
            throw new EntityNotFoundException("Menu with URL " + url + " does not exist.");
        }
    }

    private static void updateExistingMenu(MenuResponse menuResponse, Menu existingMenu) {
        existingMenu.setName(menuResponse.getName());
        existingMenu.setIcon(menuResponse.getIcon());
        existingMenu.setUrl(menuResponse.getUrl());
        existingMenu.setOnlyDev(menuResponse.getOnlyDev() != null ? menuResponse.getOnlyDev() : existingMenu.isOnlyDev());
        existingMenu.setOrder(menuResponse.getOrder());
        existingMenu.setOnIndex(menuResponse.getOnIndex() != null ? menuResponse.getOnIndex() : existingMenu.isOnIndex());
        existingMenu.setIndexOrder(menuResponse.getIndexOrder());
    }
}