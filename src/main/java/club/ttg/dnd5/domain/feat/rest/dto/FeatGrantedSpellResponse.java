package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Выдаваемое чертой заклинание с данными справочника и уровнем, с которого оно доступно.
 *
 * <p>Форма повторяет {@code SpeciesInnateSpellResponse}: у вида врождённые заклинания
 * отдаются ровно так же — запись справочника плюс требуемый уровень. Сайт рисует обе
 * таблицы одинаково, и второй формы для того же смысла заводить не за чем.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeatGrantedSpellResponse {
    @Schema(description = "Заклинание с данными справочника")
    private SpellShortResponse spell;

    /**
     * Уровень персонажа, с которого заклинание доступно. {@code null} — с момента взятия
     * черты.
     */
    @Schema(description = "Уровень персонажа, с которого доступно заклинание", example = "3")
    private Integer requiredLevel;
}
