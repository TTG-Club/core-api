package club.ttg.dnd5.domain.bastion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Пространство сооружения и цены на него из правил бастиона.
 *
 * <p>Стоимость и время постройки относятся к базовым сооружениям: специализированные не
 * покупаются, а расширяются по цене из своего описания ({@link FacilityEnlargement}).
 * Цена расширения лежит на исходном пространстве — сколько стоит перейти из него в
 * следующее. У просторного следующего нет.</p>
 */
@Getter
@AllArgsConstructor
public enum FacilitySpace {
    CRAMPED("Тесное", 4, 500, 20, 500, 25),
    ROOMY("Вместительное", 16, 1000, 45, 2000, 80),
    VAST("Просторное", 36, 3000, 125, null, null);

    private final String name;
    /** Максимальная площадь в квадратах 5×5 футов. */
    private final int squares;
    /** Стоимость постройки базового сооружения, зм. */
    private final int buildCost;
    /** Время постройки базового сооружения, дни. */
    private final int buildDays;
    /** Стоимость расширения базового сооружения до следующего пространства, зм. */
    private final Integer enlargeCost;
    /** Время расширения базового сооружения до следующего пространства, дни. */
    private final Integer enlargeDays;

    /** Следующее по размеру пространство или {@code null}, если расширять некуда. */
    public FacilitySpace next() {
        return this == VAST ? null : values()[ordinal() + 1];
    }
}
