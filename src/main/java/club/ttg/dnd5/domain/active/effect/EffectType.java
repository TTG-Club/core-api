package club.ttg.dnd5.domain.active.effect;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EffectType {
    ABILITY_BONUS_STR("ability.bonus.str", "Бонус к характеристики Сила"),
    ABILITY_BONUS_DEX("ability.bonus.dex", "Бонус к характеристики Ловкость"),
    ABILITY_BONUS_CON("ability.bonus.con", "Бонус к характеристики Телосложение"),
    ABILITY_BONUS_INT("ability.bonus.int", "Бонус к характеристики Интеллект"),
    ABILITY_BONUS_WIS("ability.bonus.wis", "Бонус к характеристики Мудрость"),
    ABILITY_BONUS_CHR("ability.bonus.chr", "Бонус к характеристики Харизма"),
    ;

    private final String key;
    private final String name;
}
