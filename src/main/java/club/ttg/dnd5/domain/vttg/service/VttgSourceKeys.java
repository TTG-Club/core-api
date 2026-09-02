package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.source.model.Source;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * Ключ источника в вокабуляре VTTG ({@code sourceKey} записи компендиума).
 *
 * <p>Правило одно на всю выгрузку намеренно: по этому же ключу к записи подбирается
 * подпись из словаря источников, который выгрузка отдаёт отдельно
 * ({@code VttgChangesResponse#sources}). Разойдись эти два места — записи остались бы
 * без названия источника, поэтому и ключ записи, и ключ словаря считаются здесь.</p>
 */
final class VttgSourceKeys {
    /** Ключ, когда источник не указан. */
    static final String FALLBACK = "srd";
    /** Поле записи с ключом источника. */
    private static final String SOURCE_KEY_FIELD = "sourceKey";
    /**
     * Поля записи с вложенными записями, у которых свой источник. Подкласс — самостоятельная
     * сущность сайта со своей книгой, а в выгрузке живёт внутри родителя; без обхода его книга
     * (UA-выпуск у воина) в словарь не попадала, и лист не мог подписать два одноимённых
     * подкласса разных выпусков.
     */
    private static final Collection<String> NESTED_FIELDS = java.util.List.of("subclasses");

    private VttgSourceKeys() {
    }

    /**
     * Ключ источника: аббревиатура в нижнем регистре.
     *
     * <p>{@code PHB24} сводится к {@code phb}: в TTG Club это отдельный источник, а в
     * вокабуляре VTTG «Книга игрока» одна.</p>
     *
     * @param source источник сущности; {@code null} — источник не задан
     */
    static String of(Source source) {
        if (source == null || !StringUtils.hasText(source.getAcronym())) {
            return FALLBACK;
        }
        if ("PHB24".equalsIgnoreCase(source.getAcronym())) {
            return "phb";
        }
        return source.getAcronym().toLowerCase(Locale.ROOT);
    }

    /**
     * Собирает ключи источников записи: её собственный и ключи вложенных записей
     * ({@link #NESTED_FIELDS}). Payload — либо дерево Jackson, либо карта (разделители черт).
     *
     * @param data payload записи.
     * @param into куда складывать найденные ключи.
     */
    static void collectSourceKeys(Object data, Collection<String> into) {
        if (data instanceof JsonNode node) {
            collect(node, into);
        } else if (data instanceof Map<?, ?> map && map.get(SOURCE_KEY_FIELD) instanceof String key) {
            into.add(key);
        }
    }

    private static void collect(JsonNode node, Collection<String> into) {
        JsonNode key = node.get(SOURCE_KEY_FIELD);
        if (key != null && key.isTextual()) {
            into.add(key.asText());
        }
        for (String field : NESTED_FIELDS) {
            JsonNode nested = node.get(field);
            if (nested != null && nested.isArray()) {
                nested.forEach(child -> collect(child, into));
            }
        }
    }
}
