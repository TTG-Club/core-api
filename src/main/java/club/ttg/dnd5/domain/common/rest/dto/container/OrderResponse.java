package club.ttg.dnd5.domain.common.rest.dto.container;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class OrderResponse <T> {
    private int order;
    private String label;
    private List<T> items;
}
