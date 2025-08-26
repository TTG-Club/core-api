package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.DamageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureDefense {
    @Schema(description = "типы урона")
    private Collection<DamageType> values;
    @Schema(description = "Текстовое описание")
    private String text;
}
