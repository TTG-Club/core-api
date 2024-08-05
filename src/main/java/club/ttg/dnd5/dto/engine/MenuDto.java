package club.ttg.dnd5.dto.engine;

import club.ttg.dnd5.model.engie.Menu;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class MenuDto {
    private String name;
    private String icon;
    private String url;
    private Boolean onlyDev;
    private Boolean external;
    private List<MenuDto> children;
    private int order;
    private Boolean onIndex;
    private Integer indexOrder;

    public MenuDto(Menu menu) {
        name = menu.getName();
        order = menu.getOrder();

        if (menu.isOnIndex()) {
            onIndex = true;
        }

        if (menu.getIndexOrder() != null) {
            indexOrder = menu.getIndexOrder();
        }

        if (menu.getIcon() != null) {
            icon = menu.getIcon();
        }

        if (menu.getUrl() != null) {
            url = menu.getUrl();
        }

        if (menu.isExternal()) {
            external = true;
        }

        if (menu.isOnlyDev()) {
            onlyDev = true;
        }

        if (!menu.getChildren().isEmpty()) {
            children = menu.getChildren()
                    .stream()
                    .map(MenuDto::new)
                    .collect(Collectors.toList());
        }
    }
}
