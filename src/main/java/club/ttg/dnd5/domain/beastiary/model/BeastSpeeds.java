package club.ttg.dnd5.domain.beastiary.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeastSpeeds {

    /**
     * Передвижение по горизонтальной поверхности
     */
    private BeastSpeed walk;
    /**
     * Копая
     */
    private BeastSpeed burrow;
    /**
     * Полетом
     */
    private BeastFlySpeed fly;

    /**
     * Плавая
     */
    private BeastSpeed swim;
    /**
     * Лазая
     */
    private BeastSpeed climb;

    public String getText() {
        var builder = new StringBuilder();
        if (walk != null) {
            builder.append(walk.getValue());
            builder.append(" фт.");
            if (walk.getText() != null) {
                builder.append(walk.getText());
            }
        }
        if (burrow != null) {
            builder.append(", копая ");
            builder.append(burrow.getValue());
            builder.append(" фт.");
            if (burrow.getText() != null) {
                builder.append(burrow.getText());
            }
        }
        if (fly != null) {
            builder.append(", летая ");
            builder.append(fly.getValue());
            builder.append(" фт.");
            if (fly.isHover()) {
                builder.append("(");
                builder.append("парит");
                builder.append(")");
            }
        }
        if (swim != null) {
            builder.append(", плавая ");
            builder.append(swim.getValue());
            builder.append(" фт.");
            if (swim.getText() != null) {
                builder.append(swim.getText());
            }
        }
        if (climb != null) {
            builder.append(", лазая ");
            builder.append(climb.getValue());
            builder.append(" фт.");
            if (climb.getText() != null) {
                builder.append(climb.getText());
            }
        }
        return builder.toString();
    }
}
