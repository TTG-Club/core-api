package club.ttg.dnd5.util;

import lombok.experimental.UtilityClass;

/**
 * Утилиты для работы с url контента в контроллерах.
 * <p>
 * Контентные url могут содержать слэши (homebrew: {@code u/{handle}/{stem}}), поэтому в контроллерах
 * они ловятся catch-all путём {@code /{*url}}. Такая переменная приходит с ведущим слэшем
 * (Spring PathPattern: {@code /api/v2/spells/fireball-phb} → {@code /fireball-phb}), а в БД url
 * хранится без него — {@link #normalizeUrl(String)} приводит значение к каноничному виду ключа.
 */
@UtilityClass
public class ContentPathUtils {

    /**
     * Убирает ведущий слэш у значения catch-all path-переменной {@code /{*url}},
     * приводя его к виду первичного ключа сущности ({@code fireball-phb}, {@code u/magistrus/fireball}).
     *
     * @param rawPathVariable значение {@code @PathVariable} из паттерна {@code /{*url}}
     * @return url без ведущего слэша (или исходное значение, если слэша нет / оно null)
     */
    public String normalizeUrl(String rawPathVariable) {
        if (rawPathVariable == null) {
            return null;
        }
        return rawPathVariable.startsWith("/")
                ? rawPathVariable.substring(1)
                : rawPathVariable;
    }
}
