package club.ttg.dnd5.domain.user.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Генератор случайного отображаемого имени в тематике D&D («Отважный Странник»).
 * Используется как дефолт при первом появлении пользователя, чтобы поле не было
 * пустым и человек пошёл менять его на своё.
 */
@Component
public class DisplayNameGenerator {
    private static final List<String> ADJECTIVES = List.of(
            "Отважный", "Хмурый", "Мудрый", "Тихий", "Дерзкий", "Странствующий",
            "Забытый", "Пламенный", "Ледяной", "Теневой", "Лунный", "Грозовой",
            "Безымянный", "Вечный", "Хитрый", "Стальной", "Древний", "Багряный",
            "Северный", "Одинокий");

    private static final List<String> NOUNS = List.of(
            "Странник", "Голем", "Гоблин", "Дракон", "Маг", "Рыцарь", "Бард",
            "Друид", "Плут", "Следопыт", "Варвар", "Паладин", "Чародей", "Жрец",
            "Искатель", "Хранитель", "Пилигрим", "Наёмник", "Алхимик", "Скиталец");

    /**
     * Возвращает случайное имя вида «Прилагательное Существительное»
     * (максимум ~23 символа — укладывается в лимит имени).
     */
    public String nextBaseName() {
        String adjective = ADJECTIVES.get(ThreadLocalRandom.current().nextInt(ADJECTIVES.size()));
        return adjective + " " + nextNoun();
    }

    /**
     * Возвращает случайное существительное — короткая основа для развода коллизий
     * с числовым суффиксом (укладывается в лимит длины).
     */
    public String nextNoun() {
        return NOUNS.get(ThreadLocalRandom.current().nextInt(NOUNS.size()));
    }

    /**
     * Возвращает случайный числовой суффикс для развода коллизий имён (например «734»).
     */
    public int nextSuffix() {
        return ThreadLocalRandom.current().nextInt(1, 10_000);
    }
}
