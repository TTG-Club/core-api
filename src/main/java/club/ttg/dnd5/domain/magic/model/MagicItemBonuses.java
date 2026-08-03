package club.ttg.dnd5.domain.magic.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Числовые бонусы, которые магия даёт поверх немагического предмета.
 *
 * <p>Заполняются в мастерской и используются листом персонажа (бонус к атаке и урону
 * связанного оружия, бонус к КД связанного доспеха или самостоятельного предмета вроде
 * плаща защиты), а также экспортом в VTTG ({@code magicBonus}). Ноль означает «бонуса нет»;
 * у проклятых предметов бонус отрицательный.</p>
 */
@Getter
@Setter
public class MagicItemBonuses {
    @Schema(description = "Бонус к броскам атаки; 0 — нет")
    private int attack;

    @Schema(description = "Бонус к урону; 0 — нет")
    private int damage;

    @Schema(description = "Бонус к классу доспеха; 0 — нет")
    private int armorClass;
}
