package club.ttg.dnd5.domain.menu.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponse {
    private String name;
    private String icon;
    private String url;
    private Boolean onlyDev;
    private List<MenuResponse> children;
    private int order;
    private Boolean onIndex;
    private Integer indexOrder;
}
