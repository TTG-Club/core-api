package club.ttg.dnd5.domain.spell.model.enums;

import lombok.Getter;

/**
 * Форма области воздействия.
 *
 * <p>Порядок подписей в {@code units} соответствует порядку значений области
 * ({@code AreaOfEffect.value1}, затем {@code value2}) и подписям полей формы
 * справочника: первое значение — «радиус или длина», второе — «высота или ширина».
 * У цилиндра они когда-то стояли наоборот, из-за чего выгрузка в VTTG принимала
 * за радиус высоту.</p>
 */
@Getter
public enum AreaOfEffectType {
    CYLINDER("цилиндр", "радиус", "высота"),
    CONE("конус", "длина"),
    CUBE("куб", "длина стороны"),
    EMANATION("эманация", "расстояние"),
    LINE("линия", "длина","ширина"),
    SPHERE("сфера", "радиус");

    private final String name;
    private final String[] units;

    AreaOfEffectType(String name, String... units) {
        this.name = name;
        this.units = units;
    }
}
