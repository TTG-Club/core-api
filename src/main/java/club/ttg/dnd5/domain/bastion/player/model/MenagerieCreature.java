package club.ttg.dnd5.domain.bastion.player.model;

import club.ttg.dnd5.domain.bastion.model.MenagerieBeast;
import club.ttg.dnd5.domain.common.dictionary.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Существо в вольерах зверинца. Лежит в JSONB сооружения и, пока его вербуют, — в приказе.
 *
 * <p>Существа зверинца считаются защитниками бастиона; игрок может вывести отдельное
 * существо из защитников — тогда его не призывают на защиту, оно действует по своей
 * природе.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class MenagerieCreature {
    /** Ключ существа в зверинце: по нему его выводят из защитников и убирают. */
    private String id;

    /** Существо из таблицы зверинца; {@code null} — существо не из таблицы, с согласия Мастера. */
    private MenagerieBeast beast;

    private String name;

    private Size size;

    /** Показатель опасности существа не из таблицы. */
    private String challenge;

    /** Сколько стоила вербовка, зм. */
    private int cost;

    /** Считается защитником бастиона. */
    private boolean defender = true;
}
