package club.ttg.dnd5.domain.bastion.model;

import club.ttg.dnd5.domain.common.dictionary.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Существа из таблицы «Существа Рощи Изумрудного Анклава». Приглашение бесплатное, но
 * существо принимает его только при чётном броске.
 *
 * <p>Имена констант лежат в приказах и в существах сооружения, поэтому переименовывать их
 * молча нельзя.</p>
 */
@Getter
@AllArgsConstructor
public enum GroveCreature {
    AWAKENED_SHRUB("Пробуждённый куст", Size.SMALL),
    AWAKENED_TREE("Пробуждённое дерево", Size.HUGE),
    BLINK_DOG("Мерцающий пёс", Size.MEDIUM),
    CENTAUR_TROOPER("Кентавр-воин", Size.LARGE),
    DRYAD("Дриада", Size.MEDIUM),
    MYCONID_ADULT("Миконид взрослый", Size.MEDIUM),
    MYCONID_SPROUT("Миконид-росток", Size.SMALL),
    PIXIE("Пикси", Size.TINY),
    SATYR("Сатир", Size.MEDIUM),
    SPRITE("Спрайт", Size.TINY),
    TREANT("Трент", Size.LARGE),
    UNICORN("Единорог", Size.LARGE);

    private final String name;
    private final Size size;
}
