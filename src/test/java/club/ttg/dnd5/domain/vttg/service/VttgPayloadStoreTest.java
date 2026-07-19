package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.vttg.model.VttgExport;
import club.ttg.dnd5.domain.vttg.repository.VttgEntityRef;
import club.ttg.dnd5.domain.vttg.repository.VttgExportRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgChange;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VttgPayloadStoreTest {
    private static final String TYPE = "spells";

    private final VttgExportRepository repository = mock(VttgExportRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VttgPayloadStore store =
            new VttgPayloadStore(repository, objectMapper, mock(PlatformTransactionManager.class));

    private final Function<String, String> identity = url -> url;
    private final Function<String, Object> toDto = url -> Map.of("name", url);

    @Test
    void freshPayloadUsedWithoutRecompute() {
        Instant changedAt = Instant.parse("2026-06-01T00:00:00Z");
        when(repository.findByTypeAndUrlIn(eq(TYPE), any()))
                .thenReturn(List.of(stored("fireball", node("stored"), changedAt, VttgPayloadStore.SCHEMA_VERSION)));
        AtomicBoolean recomputed = new AtomicBoolean(false);

        List<VttgChange> changes = store.load(TYPE, refs(ref("fireball", changedAt)),
                () -> null, trackedByUrls(recomputed), identity, toDto);

        assertEquals(1, changes.size());
        assertEquals(node("stored"), changes.get(0).data());
        assertFalse(recomputed.get(), "свежий payload не должен пересчитываться");
        verify(repository, never()).saveAll(any());
    }

    @Test
    void staleSrcTimestampTriggersRecomputeAndSave() {
        when(repository.findByTypeAndUrlIn(eq(TYPE), any()))
                .thenReturn(List.of(stored("fireball", node("old"),
                        Instant.parse("2026-05-01T00:00:00Z"), VttgPayloadStore.SCHEMA_VERSION)));
        Instant changedAt = Instant.parse("2026-06-01T00:00:00Z");

        List<VttgChange> changes = store.load(TYPE, refs(ref("fireball", changedAt)),
                () -> null, urls -> List.copyOf(urls), identity, toDto);

        assertEquals(1, changes.size());
        assertEquals(objectMapper.valueToTree(Map.of("name", "fireball")), changes.get(0).data());

        ArgumentCaptor<List<VttgExport>> captor = captor();
        verify(repository).saveAll(captor.capture());
        VttgExport saved = captor.getValue().get(0);
        assertEquals(changedAt, saved.getSrcUpdatedAt());
        assertEquals(VttgPayloadStore.SCHEMA_VERSION, saved.getSchemaVer());
    }

    @Test
    void schemaVersionMismatchTriggersRecompute() {
        Instant changedAt = Instant.parse("2026-06-01T00:00:00Z");
        when(repository.findByTypeAndUrlIn(eq(TYPE), any()))
                .thenReturn(List.of(stored("fireball", node("old"), changedAt, VttgPayloadStore.SCHEMA_VERSION - 1)));
        AtomicBoolean recomputed = new AtomicBoolean(false);

        store.load(TYPE, refs(ref("fireball", changedAt)), () -> null, trackedByUrls(recomputed), identity, toDto);

        assertTrue(recomputed.get(), "несовпадение версии схемы должно пересчитывать payload");
        verify(repository).saveAll(any());
    }

    @Test
    void newerDependencyStampInvalidatesPayload() {
        Instant changedAt = Instant.parse("2026-06-01T00:00:00Z");
        Instant dependencyStamp = Instant.parse("2026-06-10T00:00:00Z");
        // payload сохранён по собственному времени сущности — но зависимость изменилась позже.
        when(repository.findByTypeAndUrlIn(eq(TYPE), any()))
                .thenReturn(List.of(stored("flametongue", node("old"), changedAt, VttgPayloadStore.SCHEMA_VERSION)));
        AtomicBoolean recomputed = new AtomicBoolean(false);

        store.load(TYPE, refs(ref("flametongue", changedAt)), () -> dependencyStamp,
                trackedByUrls(recomputed), identity, toDto);

        assertTrue(recomputed.get(), "правка зависимости должна инвалидировать payload");
        ArgumentCaptor<List<VttgExport>> captor = captor();
        verify(repository).saveAll(captor.capture());
        assertEquals(dependencyStamp, captor.getValue().get(0).getSrcUpdatedAt(),
                "сохраняется отметка зависимости как актуальная");
    }

    @Test
    void vanishedEntitySkippedWithoutError() {
        Instant changedAt = Instant.parse("2026-06-01T00:00:00Z");
        when(repository.findByTypeAndUrlIn(eq(TYPE), any())).thenReturn(List.of());

        List<VttgChange> changes = store.load(TYPE, refs(ref("ghost", changedAt)),
                () -> null, urls -> List.of(), identity, toDto);

        assertTrue(changes.isEmpty());
    }

    @Test
    void arrayPayloadFlattensIntoOneChangePerElementKeyedById() {
        Instant changedAt = Instant.parse("2026-06-01T00:00:00Z");
        // Сущность раскрылась в несколько записей — payload сохранён массивом.
        Function<String, Object> toList = url -> List.of(
                Map.of("id", "dwarven-plate-dmg", "name", "Латы дварфов"),
                Map.of("id", "dwarven-plate-half-plate-dmg", "name", "Полулаты дварфов"));
        when(repository.findByTypeAndUrlIn(eq(TYPE), any())).thenReturn(List.of());

        List<VttgChange> changes = store.load(TYPE, refs(ref("dwarven-plate", changedAt)),
                () -> null, urls -> List.copyOf(urls), identity, toList);

        assertEquals(2, changes.size());
        // Каждый элемент массива — отдельное изменение с url = его id.
        assertEquals("dwarven-plate-dmg", changes.get(0).url());
        assertEquals("dwarven-plate-half-plate-dmg", changes.get(1).url());
        assertEquals(changedAt, changes.get(0).updatedAt());
        com.fasterxml.jackson.databind.JsonNode secondData =
                (com.fasterxml.jackson.databind.JsonNode) changes.get(1).data();
        assertEquals("Полулаты дварфов", secondData.get("name").asText());
    }

    @Test
    void brokenEntityMappingSkippedOthersSurvive() {
        Instant changedAt = Instant.parse("2026-06-01T00:00:00Z");
        when(repository.findByTypeAndUrlIn(eq(TYPE), any())).thenReturn(List.of());
        Function<String, Object> failingToDto = url -> {
            if (url.equals("broken")) {
                throw new IllegalStateException("null там, где не ждали");
            }
            return Map.of("name", url);
        };

        List<VttgChange> changes = store.load(TYPE, refs(ref("fireball", changedAt), ref("broken", changedAt)),
                () -> null, urls -> List.copyOf(urls), identity, failingToDto);

        assertEquals(1, changes.size(), "битая сущность пропускается, остальные выгружаются");
        assertEquals("fireball", changes.get(0).url());
        ArgumentCaptor<List<VttgExport>> captor = captor();
        verify(repository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size(), "payload битой сущности не сохраняется");
    }

    @Test
    void batchHydrationFailureFallsBackToPerUrlAndSkipsBroken() {
        Instant changedAt = Instant.parse("2026-06-01T00:00:00Z");
        when(repository.findByTypeAndUrlIn(eq(TYPE), any())).thenReturn(List.of());
        // Пакетная гидрация падает целиком (битый jsonb одной записи валит всю выборку) —
        // поштучный фолбэк выгружает живые сущности, битая пропускается.
        Function<Collection<String>, List<String>> byUrls = urls -> {
            if (urls.size() > 1) {
                throw new IllegalStateException("битый jsonb в пакете");
            }
            String url = urls.iterator().next();
            if (url.equals("broken")) {
                throw new IllegalStateException("битый jsonb");
            }
            return List.of(url);
        };

        List<VttgChange> changes = store.load(TYPE, refs(ref("fireball", changedAt), ref("broken", changedAt)),
                () -> null, byUrls, identity, toDto);

        assertEquals(1, changes.size(), "поштучный фолбэк спасает живые сущности");
        assertEquals("fireball", changes.get(0).url());
    }

    @Test
    void persistFailureDoesNotFailLoad() {
        Instant changedAt = Instant.parse("2026-06-01T00:00:00Z");
        when(repository.findByTypeAndUrlIn(eq(TYPE), any())).thenReturn(List.of());
        when(repository.saveAll(any())).thenThrow(new IllegalStateException("duplicate key"));

        List<VttgChange> changes = store.load(TYPE, refs(ref("fireball", changedAt)),
                () -> null, urls -> List.copyOf(urls), identity, toDto);

        assertEquals(1, changes.size(), "сбой записи кэша payload не валит ответ — он собран из памяти");
    }

    @Test
    void emptyWindowReturnsEmptyWithoutQuery() {
        List<VttgChange> changes = store.load(TYPE, List::of, () -> null, urls -> List.of(), identity, toDto);

        assertTrue(changes.isEmpty());
        verify(repository, never()).findByTypeAndUrlIn(anyString(), any());
    }

    private Function<Collection<String>, List<String>> trackedByUrls(AtomicBoolean flag) {
        return urls -> {
            flag.set(true);
            return List.copyOf(urls);
        };
    }

    private Supplier<List<VttgEntityRef>> refs(VttgEntityRef... refs) {
        return () -> List.of(refs);
    }

    private VttgEntityRef ref(String url, Instant changedAt) {
        return new VttgEntityRef() {
            @Override
            public String getUrl() {
                return url;
            }

            @Override
            public Instant getChangedAt() {
                return changedAt;
            }
        };
    }

    private VttgExport stored(String url, com.fasterxml.jackson.databind.JsonNode payload, Instant srcUpdatedAt, int schemaVer) {
        return new VttgExport(TYPE, url, payload, srcUpdatedAt, schemaVer);
    }

    private com.fasterxml.jackson.databind.JsonNode node(String name) {
        return objectMapper.valueToTree(Map.of("name", name));
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<List<VttgExport>> captor() {
        return ArgumentCaptor.forClass(List.class);
    }
}
