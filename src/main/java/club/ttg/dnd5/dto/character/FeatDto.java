package club.ttg.dnd5.dto.character;

import club.ttg.dnd5.dto.NameDto;
import club.ttg.dnd5.dto.base.NameBasedDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
public class FeatDto extends NameBasedDTO {
    private NameDto type;
}
