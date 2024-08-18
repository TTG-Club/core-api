package club.ttg.dnd5.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder

@Getter
public class TableDto {
    private String type;
    private String caption;
    private List<String> colLabels;
    private List<String> colStyles;
    private List<String> rows;
}
