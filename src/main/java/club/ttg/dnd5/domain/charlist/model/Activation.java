package club.ttg.dnd5.domain.charlist.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Активация
 */
@Getter
@Setter
public class Activation {
    private ActivationType type;
    private byte time;
}
