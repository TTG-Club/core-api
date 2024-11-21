package club.ttg.dnd5.dto.character;

import club.ttg.dnd5.dto.NameDto;
import club.ttg.dnd5.dto.base.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
public class FeatDto extends BaseDTO {
    private NameDto category;
    private String prerequisite;
}
