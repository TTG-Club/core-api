package club.ttg.dnd5.domain.beastiary.model.spellcasting;

import club.ttg.dnd5.domain.common.dictionary.RechargeType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Порция блока: список заклинаний под одним ограничением применений — заголовок
 * «По желанию», «2/день каждое», «1/день» из статблока.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatureSpellGroup {
    @Schema(description = "Чем ограничены применения порции")
    private CreatureSpellUsageMode mode;

    /**
     * Число применений. Смысл задаёт режим: у {@code PER_*_EACH} это применения КАЖДОМУ
     * заклинанию порции, у {@code PER_*_POOL} — применения на ВСЮ порцию.
     */
    @Schema(description = "Число применений порции", example = "2")
    private Integer count;

    @Schema(description = "Какой отдых возвращает применения")
    private CreatureSpellRestKind rest;

    /** Тот же словарь перезарядки, что у {@code CreatureAction.recharge}; только у {@code RECHARGE}. */
    @Schema(description = "Перезарядка порции")
    private RechargeType recharge;

    @Schema(description = "Своя подпись порции вместо выведенной из режима", example = "1/день")
    private String label;

    @Schema(description = "Заклинания порции")
    private List<CreatureSpellRef> spells;
}
