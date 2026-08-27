package club.ttg.dnd5.domain.common.rest.dto;

import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Выдаваемое записью справочника заклинание: запись каталога плюс уровень, с которого оно
 * доступно.
 *
 * <p>Форма повторяет {@code SpeciesInnateSpellResponse}: у вида врождённые заклинания
 * отдаются ровно так же. Одна форма на всех, кто выдаёт заклинания — черта, предыстория,
 * умение класса, — потому что сайт рисует их одной таблицей.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrantedSpellResponse {
    @Schema(description = "Заклинание с данными справочника")
    private SpellShortResponse spell;

    /**
     * Уровень персонажа, с которого заклинание доступно. {@code null} — с момента взятия
     * черты.
     */
    @Schema(description = "Уровень персонажа, с которого доступно заклинание", example = "3")
    private Integer requiredLevel;
}
