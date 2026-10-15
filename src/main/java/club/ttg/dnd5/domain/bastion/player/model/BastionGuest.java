package club.ttg.dnd5.domain.bastion.player.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Гость бастиона из событий «Гость» и «Беженцы». Живёт в JSONB бастиона, пока не уедет,
 * не погибнет или его не отошлют.
 */
@Getter
@Setter
@NoArgsConstructor
public class BastionGuest {
    private String id;

    private Kind kind;

    /** Кто это: «Беженцы», «Наёмник», «Дружественный монстр». */
    private String name;

    /** Сколько их: у беженцев 2к4, у остальных 1. */
    private int count = 1;

    /** Ход, в который гость прибыл. */
    private int arrivedOnTurn;

    /** Ход, на котором гость уедет сам; {@code null} — пока не отошлют. */
    private Integer leavesOnTurn;

    /** Кто гостит. */
    @Getter
    @AllArgsConstructor
    public enum Kind {
        /** Известная личность: уезжает через 7 дней и оставляет рекомендательное письмо. */
        CELEBRITY("Известная личность"),
        /** Просит убежища: уезжает через 7 дней. */
        SANCTUARY("Просит убежища"),
        /** Наёмник — дополнительный защитник бастиона. */
        DEFENDER("Наёмник-защитник"),
        /** Дружественный монстр: защищает бастион от 1 атаки. */
        MONSTER("Дружественный монстр"),
        /** Беженцы: уходят, когда им найдут дом или бастион атакуют. */
        REFUGEES("Беженцы");

        private final String name;
    }
}
