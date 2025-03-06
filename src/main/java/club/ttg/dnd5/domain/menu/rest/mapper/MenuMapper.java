package club.ttg.dnd5.domain.menu.rest.mapper;

import club.ttg.dnd5.domain.menu.rest.dto.MenuResponse;
import club.ttg.dnd5.domain.menu.model.Menu;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MenuMapper {
    MenuMapper INSTANCE = Mappers.getMapper(MenuMapper.class);

    @Mapping(source = "children", target = "children")
    MenuResponse menuToMenuApi(Menu menu);

    @Mapping(source = "children", target = "children")
    Menu menuApiToMenu(MenuResponse menuResponse);

    List<MenuResponse> menusToMenuApis(List<Menu> menus);

    List<Menu> menuApisToMenus(List<MenuResponse> menuResponses);
}