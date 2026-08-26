package club.ttg.dnd5.domain.item.model.tool;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Параметры инструмента — то, чем инструмент отличается от прочего снаряжения на листе
 * персонажа и на виртуальном столе.
 *
 * <p>Все поля необязательны: пустое значение означает «вывести как раньше» —
 * категорию из типов предмета, базовый инструмент из адреса страницы. Значения —
 * вокабуляр VTTG строками, как у {@code Weapon}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tool {
    /**
     * Категория инструмента: {@code artisan}, {@code gaming}, {@code musical},
     * {@code other}. Пусто — выводится из типов предмета.
     */
    private String category;

    /**
     * Ключ инструмента в справочнике листа ({@code thieves-tools}). По нему сверяется
     * владение инструментом. Пусто — выводится из адреса страницы.
     */
    private String baseType;

    /**
     * Характеристика проверки инструментом ({@code strength}…{@code charisma}).
     * Пусто — по правилам проверки.
     */
    private String ability;

    /** Собственный бонус инструмента к проверке. */
    private Integer bonus;

    /**
     * Режим учёта владения: {@code auto}, {@code none}, {@code half},
     * {@code proficient}, {@code expertise}. Пусто — {@code auto}.
     */
    private String proficiencyMode;
}
