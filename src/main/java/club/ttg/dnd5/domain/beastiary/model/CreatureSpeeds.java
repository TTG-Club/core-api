package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.beastiary.model.speed.FlySpeed;
import club.ttg.dnd5.domain.beastiary.model.speed.Speed;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

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
}
