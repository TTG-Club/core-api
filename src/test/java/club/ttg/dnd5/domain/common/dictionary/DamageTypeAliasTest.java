package club.ttg.dnd5.domain.common.dictionary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Огненный урон переименован из {@code FAIR} в {@code FIRE}, но старое значение осталось
 * в сохранённых данных: в jsonb-колонках, в листах персонажей и в чужих закладках.
 * <p>
 * Читаться оно обязано и без миграции данных — иначе выкатка кода ломает всё, что ещё
 * не переписано. Тест берёт «голый» {@link ObjectMapper}, как у Hibernate-типа jsonb,
 * а не настроенный Spring-ом: алиас должен работать сам по себе.
 */
class DamageTypeAliasTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @DisplayName("прежнее имя FAIR читается как FIRE")
    void readsLegacyName() throws Exception {
        assertEquals(DamageType.FIRE, mapper.readValue("\"FAIR\"", DamageType.class));
    }

    @Test
    @DisplayName("новое имя FIRE читается как FIRE")
    void readsCurrentName() throws Exception {
        assertEquals(DamageType.FIRE, mapper.readValue("\"FIRE\"", DamageType.class));
    }

    @Test
    @DisplayName("массив со старым значением внутри jsonb читается целиком")
    void readsLegacyNameInsideArray() throws Exception {
        List<DamageType> types = mapper.readValue(
                "[\"FAIR\",\"COLD\"]", new TypeReference<List<DamageType>>() {});

        assertEquals(List.of(DamageType.FIRE, DamageType.COLD), types);
    }

    @Test
    @DisplayName("наружу пишется только новое имя")
    void writesCurrentName() throws Exception {
        assertEquals("\"FIRE\"", mapper.writeValueAsString(DamageType.FIRE));
    }
}
