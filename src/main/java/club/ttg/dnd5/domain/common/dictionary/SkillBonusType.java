package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SkillBonusType {
    NORMAL("Бонус характеристики + БМ"),
    DOUBLED("Бонус характеристики + удвоенный БМ"),;

    private final String name;
}
