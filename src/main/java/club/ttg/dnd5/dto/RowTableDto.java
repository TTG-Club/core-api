package club.ttg.dnd5.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Getter
@Setter
public class RowTableDto {
    private String type;
}
