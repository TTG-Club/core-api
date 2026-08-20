package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

/**
 * Один список заклинаний, добавляемых чертой в список класса, — с уровнем доступа,
 * количеством и записями справочника.
 *
 * <p>Записи справочника нужны именно здесь: круг заклинания в механике не хранится, а без
 * круга сайту таблицу «Заклинания метки» не собрать. Форма повторяет
 * {@link FeatGrantedSpellResponse} по замыслу — механика плюс дополненная запись, — но
 * своим типом: там уровень у ОДНОГО заклинания, здесь у целого списка.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeatSpellListGroupResponse {
    /**
     * Уровень персонажа, с которого список открывается. {@code null} — с момента взятия
     * черты.
     */
    @Schema(description = "Уровень персонажа, с которого список открывается", example = "5")
    private Integer requiredLevel;

    /**
     * Сколько заклинаний из списка игрок берёт: формула той же грамматики, что у максимума
     * ресурса. {@code null} — весь список целиком.
     */
    @Schema(description = "Сколько заклинаний берут; пусто — весь список", example = "@prof")
    private String count;

    @Schema(description = "Заклинания списка с данными справочника")
    private Collection<SpellShortResponse> spells;
}
