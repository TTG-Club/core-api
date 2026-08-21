package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryQueryRequest;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlossaryPredicateBuilderTest {

    /**
     * Список глоссария отдаёт фильтр источников, поэтому выдача обязана его учитывать:
     * без этого выбор книг в панели ничего не менял.
     */
    @Test
    void sourcesNarrowResults() {
        GlossaryQueryRequest request = new GlossaryQueryRequest();
        request.setSource(Set.of("PHB"));

        String predicate = GlossaryPredicateBuilder.build(request).toString();

        assertTrue(predicate.contains("glossary.source"));
        assertTrue(predicate.contains("PHB"));
    }

    /** Источники не выбраны — выдачу сужать нечем, условие не добавляется. */
    @Test
    void emptySourcesDoNotNarrowResults() {
        GlossaryQueryRequest request = new GlossaryQueryRequest();

        String predicate = GlossaryPredicateBuilder.build(request).toString();

        assertFalse(predicate.contains("glossary.source"));
    }
}
