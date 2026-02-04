package club.ttg.dnd5.domain.beastiary.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Builder
@Getter
public class BonusDto {
    private String label;
    private String value;
    private String text;
}
