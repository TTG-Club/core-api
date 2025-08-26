package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureImmunities {
    @Schema(description = "Иммунитеты к типам урона")
    private Collection<DamageType> damage;
    @Schema(description = "Иммунитеты к состояниям")
    private Collection<Condition> condition;

    @Schema(description = "Текстовое описание")
    private String text;
}
