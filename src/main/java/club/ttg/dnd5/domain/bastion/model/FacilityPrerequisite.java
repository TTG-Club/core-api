package club.ttg.dnd5.domain.bastion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Требование для возведения специализированного сооружения (помимо уровня).
 *
 * <p>Разобрано в перечисление, чтобы мини-игра могла проверить его по листу персонажа;
 * отсутствие требования — {@code null} в сооружении.</p>
 */
@Getter
@AllArgsConstructor
public enum FacilityPrerequisite {
    ARCANE_FOCUS_OR_TOOL("Умение использовать заклинательную фокусировку или инструмент в качестве неё"),
    SPELLCASTING_FOCUS("Умение использовать заклинательную фокусировку"),
    HOLY_SYMBOL_OR_DRUIDIC_FOCUS("Умение использовать священный символ или фокусировку друида в качестве заклинательной фокусировки"),
    EXPERTISE("Компетентность в любом навыке"),
    FIGHTING_STYLE_OR_UNARMORED_DEFENSE("Умение Боевой стиль или Защита без доспехов");

    private final String name;
}
