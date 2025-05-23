package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.beastiary.model.speed.FlySpeed;
import club.ttg.dnd5.domain.beastiary.model.speed.Speed;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
@Setter
public class CreatureSpeeds {

    /**
     * Передвижение по горизонтальной поверхности
     */
    private Collection<Speed> walk;
    /**
     * Копая
     */
    private Collection<Speed> burrow;
    /**
     * Полетом
     */
    private Collection<FlySpeed> fly;

    /**
     * Плавая
     */
    private Collection<Speed> swim;
    /**
     * Лазая
     */
    private Collection<Speed> climb;

    public String getText() {
        var builder = new StringBuilder();
        getSpeedText(builder, walk, burrow);
        builder.append(fly.stream().map(s ->
                "%d фт.%s"
                        .formatted(s.getValue(),
                                StringUtils.hasText(s.getText()) ? "("
                                        + (s.isHover() ? "парит; " : "")
                                        + s.getText() +")" : "")
        ).collect(Collectors.joining(", ")));
        getSpeedText(builder, swim, climb);
        return builder.toString();
    }

    private void getSpeedText(final StringBuilder builder,
                              final Collection<Speed> swim,
                              final Collection<Speed> climb) {
        builder.append(swim.stream().map(s ->
                "%d фт.%s"
                        .formatted(s.getValue(),
                                StringUtils.hasText(s.getText()) ? "(" + s.getText() +")" : "")
        ).collect(Collectors.joining(", ")));
        builder.append(climb.stream().map(s ->
                "%d фт.%s"
                        .formatted(s.getValue(),
                                StringUtils.hasText(s.getText()) ? "(" + s.getText() +")" : "")
        ).collect(Collectors.joining(", ")));
    }
}
