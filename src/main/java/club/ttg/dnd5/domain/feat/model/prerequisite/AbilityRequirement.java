package club.ttg.dnd5.domain.feat.model.prerequisite;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * Требование к значению характеристики.
 *
 * <p>{@link #anyOf} — именно «любая из», а не «все»: в книгах это всегда формулируется
 * как «Сила или Ловкость 13+».</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbilityRequirement {
    @Schema(description = "Характеристики, любой из которых достаточно",
            examples = {"STRENGTH", "DEXTERITY"})
    private Set<Ability> anyOf;

    @Schema(description = "Минимальное значение характеристики", example = "13")
    private Integer minValue;
}
