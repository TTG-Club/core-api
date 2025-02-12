package club.ttg.dnd5.dto.spell.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("components")
public class SpellComponentsDto {

    @JsonProperty("v")
    private boolean verbal; // Вербальный компонент

    @JsonProperty("s")
    private boolean somatic; // Соматический компонент

    @JsonProperty("m")
    private List<SpellMaterialComponentDto> material; // Материальные компоненты
}

