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
    private static final MenuMapper menuMapper = MenuMapper.INSTANCE;
    private final MenuRepository menuRepository;

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

    @Transactional
    public MenuResponse update(String oldUrl, MenuResponse menuResponse) {
        if (menuRepository.existsByUrl(oldUrl)) {
            if (!oldUrl.equals(menuResponse.getUrl())) {
                menuRepository.deleteByUrl(oldUrl);
            }

            Menu updatedMenu = menuMapper.menuApiToMenu(menuResponse);

            if (menuResponse.getChildren() != null && !menuResponse.getChildren().isEmpty()) {
                List<Menu> updatedChildren = menuResponse.getChildren().stream()
                        .map(menuMapper::menuApiToMenu)
                        .peek(child -> child.setParent(updatedMenu))
                        .toList();
                updatedMenu.setChildren(updatedChildren);
            }

            Menu savedMenu = menuRepository.save(updatedMenu);

            return menuMapper.menuToMenuApi(savedMenu);
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
}