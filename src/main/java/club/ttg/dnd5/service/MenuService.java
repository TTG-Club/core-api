package club.ttg.dnd5.service;

import club.ttg.dnd5.dto.engine.MenuApi;
import club.ttg.dnd5.exception.StorageException;
import club.ttg.dnd5.mapper.engine.MenuMapper;
import club.ttg.dnd5.model.engine.Menu;
import club.ttg.dnd5.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private static final MenuMapper menuMapper = MenuMapper.INSTANCE;

    public List<MenuApi> findAll() {
        return menuRepository.findAll().stream()
                .map(menuMapper::menuToMenuApi)
                .collect(Collectors.toList());
    }

    public Optional<MenuApi> findByUrl(String url) {
        return menuRepository.findByUrl(url)
                .map(menuMapper::menuToMenuApi);
    }

    public MenuApi save(MenuApi menuApi) {
        Menu menu = menuMapper.menuApiToMenu(menuApi);
        Menu savedMenu = menuRepository.save(menu);
        return menuMapper.menuToMenuApi(savedMenu);
    }

    public MenuApi update(MenuApi menuApi) {
        if (menuRepository.existsByUrl(menuApi.getUrl())) {
            Menu menu = menuMapper.menuApiToMenu(menuApi);
            Menu updatedMenu = menuRepository.save(menu);
            return menuMapper.menuToMenuApi(updatedMenu);
        } else {
            throw new StorageException("Menu with URL " + menuApi.getUrl() + " does not exist.");
        }
    }

    public void deleteByUrl(String url) {
        if (menuRepository.existsByUrl(url)) {
            menuRepository.deleteByUrl(url);
        } else {
            throw new StorageException("Menu with URL " + url + " does not exist.");
        }
    }
}