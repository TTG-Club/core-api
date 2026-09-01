package club.ttg.dnd5.domain.magic.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.domain.item.model.weapon.DamagePart;
import club.ttg.dnd5.domain.magic.model.MagicItemBonuses;
import club.ttg.dnd5.domain.magic.model.mechanics.MagicItemMechanics;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MagicItemDetailResponse extends BaseResponse {
    @Schema(description = "Подзаголовок")
    private String subtitle;

    @Schema(description = "Механика влияния на лист персонажа; null — предмет её не описывает")
    private MagicItemMechanics mechanics;

    /**
     * Свойства ниже страница раздела показывает блоком, а не текстом описания. Пустые
     * не отдаём вовсе ({@code NON_EMPTY}): у большинства записей структуры нет, и блок
     * им рисовать нечем.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Бонусы поверх немагического предмета: к атаке, к урону и к КД")
    private MagicItemBonuses bonuses;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Дополнительный урон, который магия добавляет к броску немагической основы")
    private List<DamagePart> damageParts;

    @Schema(description = "true если предмет — заклинательная фокусировка")
    private boolean focus;

    @Schema(description = "true если предмет адамантиновый")
    private boolean adamantine;
}
