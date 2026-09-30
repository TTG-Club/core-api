package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.beastiary.model.action.CreatureActionEffect;
import club.ttg.dnd5.domain.common.dictionary.RechargeType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
@Getter
@Setter
public class CreatureTrait {
    private String name;

    private String english;

    private String description;

    private String original;

    private RechargeType recharge;

    /**
     * Боевая механика умения — та же, что у действия: умение тоже бывает
     * бросаемым («Облако слизи» — спасбросок с уроном). Не задана — в выгрузку
     * VTTG уезжает одно описание.
     */
    private CreatureActionEffect effect;

    /**
     * «Легендарное сопротивление»: сколько раз в день (до долгого отдыха) существо
     * может заменить проваленный спасбросок успехом. Целое 1–20; {@code null} —
     * возможности нет.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer saveSuccessPerDay;
}