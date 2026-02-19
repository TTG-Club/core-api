package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Delimiter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class PrimaryAbilitiesDto {
    @Schema(description = "Основные характеристики")
    private Set<Ability> values;
    @Schema(description = "Разделитель для основные характеристик")
    private Delimiter delimiter;
}
