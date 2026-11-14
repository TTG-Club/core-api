package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.common.url.CatalogUrlRenamer;
import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import club.ttg.dnd5.domain.filter.model.SourceFilterInfo;
import club.ttg.dnd5.domain.source.event.SourceAcronymChangedEvent;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.model.filter.SourceSavedFilter;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceGroupFilter;
import club.ttg.dnd5.domain.spell.model.Spell;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SourceAcronymRenamerTest {
    private final EntityManager entityManager = mock(EntityManager.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final CatalogUrlRenamer urlRenamer = mock(CatalogUrlRenamer.class);
    private final SourceAcronymRenamer renamer = new SourceAcronymRenamer(entityManager, events, urlRenamer);

    /** Запросы в порядке выполнения и их параметры. */
    private final List<String> statements = new ArrayList<>();
    private final Map<String, Map<String, Object>> parameters = new HashMap<>();
    private final List<Object> savedFilterIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        @SuppressWarnings("unchecked")
        EntityType<Spell> spell = mock(EntityType.class);
        when(spell.getJavaType()).thenReturn(Spell.class);
        Metamodel metamodel = mock(Metamodel.class);
        when(metamodel.getEntities()).thenReturn(Set.of(spell));
        when(entityManager.getMetamodel()).thenReturn(metamodel);

        when(entityManager.createNativeQuery(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            statements.add(sql);
            Map<String, Object> bound = new HashMap<>();
            parameters.put(sql, bound);
            Query query = mock(Query.class);
            when(query.setParameter(anyString(), any())).thenAnswer(set -> {
                bound.put(set.getArgument(0), set.getArgument(1));
                return query;
            });
            when(query.executeUpdate()).thenReturn(1);
            when(query.getResultList()).thenReturn(savedFilterIds);
            return query;
        });
    }

    /** Ключ меняется одним запросом, ссылки разделов — следом, в конце контекст сброшен. */
    @Test
    void renamesKeyReferencesAndClearsContext() {
        renamer.rename(source("XGE", null), "XGTE", "admin");

        assertEquals(3, statements.size());
        assertTrue(statements.get(0).contains("update source"));
        assertEquals(Map.of("newAcronym", "XGTE", "oldAcronym", "XGE", "vttgKey", "xge"),
                parameters.get(statements.get(0)));
        assertEquals("update \"spell\" set \"source\" = :newAcronym where \"source\" = :oldAcronym",
                statements.get(1));
        assertTrue(statements.get(2).contains("from saved_filter"));
        assertEquals("%\"XGE\"%", parameters.get(statements.get(2)).get("pattern"));

        InOrder order = inOrder(entityManager, urlRenamer, events);
        order.verify(entityManager).flush();
        order.verify(entityManager).createNativeQuery(statements.get(0));
        order.verify(entityManager).flush();
        order.verify(urlRenamer).rename("XGE", "XGTE", "admin");
        order.verify(entityManager).flush();
        order.verify(entityManager).clear();
        order.verify(events).publishEvent(new SourceAcronymChangedEvent("XGE", "XGTE", "admin"));
    }

    /** Повторная смена акронима не трогает уже замороженный ключ выгрузки VTTG. */
    @Test
    void keepsFrozenVttgKeyOnSecondRename() {
        renamer.rename(source("XGTE", "xge"), "XGE2", null);

        assertEquals("xge", parameters.get(statements.get(0)).get("vttgKey"));
    }

    /** Знаки подстановки LIKE в акрониме не расширяют выборку фильтров. */
    @Test
    void escapesLikeWildcards() {
        renamer.rename(source("A_B%", null), "AB", null);

        assertEquals("%\"A!_B!%\"%", parameters.get(statements.get(2)).get("pattern"));
    }

    /** В сохранённом фильтре меняется акроним, отметка пользователя остаётся. */
    @Test
    void rewritesSavedFilters() {
        UUID id = UUID.randomUUID();
        savedFilterIds.add(id);
        SourceGroupFilter.SourceFilterItem item = new SourceGroupFilter.SourceFilterItem("Занатар", "XGE", false);
        SourceSavedFilter saved = new SourceSavedFilter();
        saved.setFilter(new SourceFilterInfo(List.of(new SourceGroupFilter(List.of(item), "Базовые"))));
        when(entityManager.find(AbstractSavedFilter.class, id)).thenReturn(saved);

        renamer.rename(source("XGE", null), "XGTE", null);

        assertEquals("XGTE", item.getValue());
        assertEquals(Boolean.FALSE, item.getSelected());
    }

    /** Сбой на любом шаге уходит наружу: транзакция откатится, событие не публикуется. */
    @Test
    void failureStopsRenameWithoutEvent() {
        savedFilterIds.add(UUID.randomUUID());
        when(entityManager.find(any(), any())).thenThrow(new IllegalStateException("boom"));

        assertThrows(IllegalStateException.class, () -> renamer.rename(source("XGE", null), "XGTE", null));

        verify(entityManager, never()).clear();
        verify(events, never()).publishEvent(any());
    }

    private static Source source(String acronym, String vttgKey) {
        Source source = new Source();
        source.setAcronym(acronym);
        source.setVttgKey(vttgKey);
        return source;
    }
}
