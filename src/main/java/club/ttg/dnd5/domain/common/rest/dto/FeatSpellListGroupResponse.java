package club.ttg.dnd5.domain.common.rest.dto;

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
 * {@link GrantedSpellResponse} по замыслу — механика плюс дополненная запись, — но
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
     * Прежняя настройка «сколько берут». Расширение списка количества не спрашивает —
     * оно лишь делает заклинания доступными для подготовки, а «выбрать N из перечисленных»
     * стало выбором заклинаний с перечисленным пулом. Поле отдаётся как есть ради записей,
     * сохранённых до этого; потребители его не читают.
     */
    @Deprecated
    @Schema(description = "Устаревшее: прежнее «сколько берут»; потребители не читают", example = "@prof")
    private String count;

    @Schema(description = "Заклинания списка с данными справочника")
    private Collection<SpellShortResponse> spells;
}
