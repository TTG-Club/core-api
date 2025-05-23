package club.ttg.dnd5.domain.beastiary.model.speed;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlySpeed extends Speed {
    /**
     * Может парить (только если есть полет)
     */
    private boolean hover;
}
