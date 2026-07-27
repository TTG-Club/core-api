package club.ttg.dnd5.domain.common.service.slug;

import java.util.Locale;

/**
 * Чистые (без Spring) утилиты формы url пользовательского (homebrew) контента.
 * <p>
 * Homebrew-url живёт в отдельном неймспейсе-пути {@code u/{handle}/{stem}}, который по форме
 * не пересекается с официальными url вида {@code {stem}-{acronym}} (у официальных нет слэша и
 * они никогда не начинаются с {@code u/}). За счёт этого столкновение официального и
 * пользовательского контента невозможно by construction — reserve-list не требуется ни для
 * акронимов источников, ни для хендлов пользователей.
 * <p>
 * <b>Важно:</b> {@link #isHomebrew(String)} — это дешёвая проверка формы для роутинга/валидации,
 * а НЕ источник правды для авторизации. Принадлежность контента (официальный vs homebrew, владелец)
 * определяется колонкой {@code owner_id} сущности, а не парсингом url.
 */
public final class HomebrewSlug {

    /** Префикс-неймзспейс всех homebrew-url. Официальный контент его никогда не использует. */
    public static final String PREFIX = "u";

    /** Разделитель сегментов пути в homebrew-url. */
    public static final String SEPARATOR = "/";

    private HomebrewSlug() {
    }

    /**
     * Нормализует строку в kebab-slug: латиница/цифры/дефис, без краевых дефисов.
     * Семантика совпадает с генерацией url в остальном проекте (нелатиница схлопывается в дефисы).
     *
     * @return нормализованный slug, либо пустая строка, если полезных символов не осталось
     */
    public static String slugify(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    /** Префикс-путь владельца: {@code u/{handle}/}. Ожидает уже нормализованный handle. */
    public static String namespace(String handle) {
        return PREFIX + SEPARATOR + handle + SEPARATOR;
    }

    /**
     * Проверка формы: похоже ли на homebrew-url.
     * Не использовать для авторизации — только как подсказку роутингу/валидации.
     */
    public static boolean isHomebrew(String url) {
        return url != null && url.startsWith(PREFIX + SEPARATOR);
    }
}
