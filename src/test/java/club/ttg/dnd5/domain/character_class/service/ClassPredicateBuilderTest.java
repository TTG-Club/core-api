package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.rest.dto.ClassQueryRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Отбор классов для общего списка.
 *
 * <p>Проверяется одно: подклассы отсекает ПРЕДИКАТ, а не отбор над готовой страницей.
 * Отбор после пагинации выбрасывал записи из уже нарезанной страницы, и запрос первых
 * тридцати возвращал четыре класса из семнадцати — остальные двадцать шесть мест
 * занимали подклассы. Проверять предикат строкой некрасиво, но иначе регрессию не
 * поймать: она не в результате запроса, а в том, на каком шаге стоит условие.</p>
 */
class ClassPredicateBuilderTest {
    private static final String PARENT_IS_NULL = "characterClass.parentUrl is null";

    /** Общий список: только классы — подклассы отдаёт своя ручка. */
    @Test
    void listWithoutSearchKeepsOnlyTopLevelClasses() {
        var predicate = ClassPredicateBuilder.build(new ClassQueryRequest());

        assertTrue(predicate.toString().contains(PARENT_IS_NULL));
    }

    /**
     * Поиск по названию — исключение: игрок ищет «Мистического рыцаря», не помня, что
     * это подкласс воина.
     */
    @Test
    void searchByNameKeepsSubclasses() {
        var request = new ClassQueryRequest();
        request.setSearch("рыцарь");

        assertFalse(ClassPredicateBuilder.build(request).toString().contains(PARENT_IS_NULL));
    }

    /** Пустая строка поиска — это отсутствие поиска, а не поиск по пустоте. */
    @Test
    void blankSearchIsNoSearch() {
        var request = new ClassQueryRequest();
        request.setSearch("   ");

        assertTrue(ClassPredicateBuilder.build(request).toString().contains(PARENT_IS_NULL));
    }
}
