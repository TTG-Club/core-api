package club.ttg.dnd5.domain.common.service.slug;

import org.springframework.stereotype.Service;

import java.util.function.Predicate;

/**
 * Генератор url для пользовательского (homebrew) контента.
 * <p>
 * Собирает slug вида {@code u/{handle}/{stem}} и разрешает коллизии числовым суффиксом
 * ({@code u/{handle}/{stem}-2}, {@code -3}, ...). Уникальность проверяется переданным предикатом
 * {@code exists} (обычно {@code repository::existsById}), поэтому генератор не зависит от
 * конкретной сущности и переиспользуется всеми типами контента.
 * <p>
 * Ветку официальных url ({@code {stem}-{acronym}}) генератор не трогает — она формируется отдельно.
 */
@Service
public class HomebrewSlugService {

    /** Предохранитель от бесконечного цикла при подборе уникального суффикса. */
    private static final int MAX_ATTEMPTS = 10_000;

    /**
     * Нормализует произвольную строку в kebab-slug (см. {@link HomebrewSlug#slugify(String)}).
     */
    public String slugify(String value) {
        return HomebrewSlug.slugify(value);
    }

    /**
     * Строит уникальный homebrew-url {@code u/{handle}/{stem}} с числовым фолбэком на коллизии.
     * <p>
     * {@code handle} и {@code stem} нормализуются внутри, так что на вход можно подавать «сырые»
     * значения (например, латинское название заклинания и хендл владельца).
     *
     * @param handle хендл владельца (будет нормализован); после нормализации не должен быть пустым
     * @param stem   основа url, обычно из английского названия (будет нормализована); не пустая
     * @param exists предикат существования url (например, {@code spellRepository::existsById})
     * @return уникальный url, для которого {@code exists.test(url) == false}
     * @throws IllegalArgumentException если {@code handle} или {@code stem} пусты после нормализации
     * @throws IllegalStateException    если уникальный url не удалось подобрать за {@link #MAX_ATTEMPTS} попыток
     */
    public String generate(String handle, String stem, Predicate<String> exists) {
        String normalizedHandle = slugify(handle);
        String normalizedStem = slugify(stem);

        if (normalizedHandle.isEmpty()) {
            throw new IllegalArgumentException(
                    "Хендл владельца пуст после нормализации: '" + handle + "'");
        }
        if (normalizedStem.isEmpty()) {
            throw new IllegalArgumentException(
                    "Основа url (stem) пуста после нормализации: '" + stem + "'");
        }

        String prefix = HomebrewSlug.namespace(normalizedHandle);
        String base = prefix + normalizedStem;
        if (!exists.test(base)) {
            return base;
        }

        for (int n = 2; n <= MAX_ATTEMPTS; n++) {
            String candidate = base + "-" + n;
            if (!exists.test(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException(
                "Не удалось подобрать уникальный homebrew-url для основы '" + base + "'");
    }
}
