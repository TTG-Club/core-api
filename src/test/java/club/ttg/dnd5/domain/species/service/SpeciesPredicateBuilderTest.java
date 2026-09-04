package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.species.rest.dto.SpeciesQueryRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Отбор видов для общего списка — та же проверка, что у классов
 * ({@code ClassPredicateBuilderTest}): подвиды отсекает ПРЕДИКАТ, а не отбор над
 * готовой страницей. Отбор после пагинации выбрасывает записи из уже нарезанной
 * страницы, и запрос первых тридцати возвращал бы горстку видов — остальные места
 * заняли бы подвиды.
 */
class SpeciesPredicateBuilderTest {
    private static final String PARENT_IS_NULL = "species.parent is null";

    /** Общий список: только виды — подвиды показывает карточка самого вида. */
    @Test
    void listWithoutSearchKeepsOnlyTopLevelSpecies() {
        var predicate = SpeciesPredicateBuilder.build(new SpeciesQueryRequest());

        assertTrue(predicate.toString().contains(PARENT_IS_NULL));
    }

    /**
     * Поиск по названию — исключение: игрок ищет «Дроу», не помня, что это подвид
     * эльфа.
     */
    @Test
    void searchByNameKeepsSubspecies() {
        var request = new SpeciesQueryRequest();
        request.setSearch("дроу");

        assertFalse(SpeciesPredicateBuilder.build(request).toString().contains(PARENT_IS_NULL));
    }

    /** Пустая строка поиска — это отсутствие поиска, а не поиск по пустоте. */
    @Test
    void blankSearchIsNoSearch() {
        var request = new SpeciesQueryRequest();
        request.setSearch("   ");

        assertTrue(SpeciesPredicateBuilder.build(request).toString().contains(PARENT_IS_NULL));
    }
}
