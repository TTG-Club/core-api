package club.ttg.dnd5.domain.common.model;

import club.ttg.dnd5.domain.common.dictionary.Dice;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Roll {
    private Short diceCount;
    private Dice dice;
    private Short bonus;

    public String toString() {
        var builder = new StringBuilder();
        if (diceCount != null) {
            builder.append(diceCount);
        }
        if (dice != null) {
            builder.append(dice.getName());
        }
        if (bonus != null) {
            builder.append(" ");
            builder.append(bonus >= 0 ? "+" : "-");
            builder.append(" ");
            builder.append(bonus);
        }
        return builder.toString();
    }
}
