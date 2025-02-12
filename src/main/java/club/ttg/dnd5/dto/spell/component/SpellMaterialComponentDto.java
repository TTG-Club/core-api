package club.ttg.dnd5.dto.spell.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("material_component")
public class SpellMaterialComponentDto {

    @JsonProperty("name")
    private String name; // Название компонента

    @JsonProperty("price")
    private Integer price; // Цена (если применимо)

    @JsonProperty("comparison")
    private String comparison; // Сравнение ('<', '>', '=')

    @JsonProperty("consumable")
    private boolean consumable; // Расходуется ли при использовании
}
