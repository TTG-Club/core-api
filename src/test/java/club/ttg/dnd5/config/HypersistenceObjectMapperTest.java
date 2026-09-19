package club.ttg.dnd5.config;

import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Порядок множеств из jsonb. По умолчанию Jackson отдаёт {@code HashSet}, а у
 * перечислений {@code hashCode} берётся от адреса объекта: после перезапуска
 * приложения тот же jsonb читается в другом порядке.
 */
class HypersistenceObjectMapperTest
{
    private final ObjectMapper mapper = new HypersistenceFilterObjectMapperSupplier().get();

    @Test
    void enumSetKeepsStoredOrder() throws Exception
    {
        Set<Ability> parsed = mapper.readValue(
                "[\"STRENGTH\",\"DEXTERITY\"]", new TypeReference<Set<Ability>>() {});

        assertEquals(List.of(Ability.STRENGTH, Ability.DEXTERITY), List.copyOf(parsed));
    }

    @Test
    void reversedStoredOrderIsKeptAsIs() throws Exception
    {
        Set<Ability> parsed = mapper.readValue(
                "[\"DEXTERITY\",\"STRENGTH\"]", new TypeReference<Set<Ability>>() {});

        assertEquals(List.of(Ability.DEXTERITY, Ability.STRENGTH), List.copyOf(parsed));
    }

    /** Поле сущности, на котором расхождение и поймали. */
    @Test
    void classSavingThrowsKeepStoredOrder() throws Exception
    {
        CharacterClass parsed = mapper.readValue(
                "{\"savingThrows\":[\"STRENGTH\",\"DEXTERITY\"]}", CharacterClass.class);

        assertEquals(List.of(Ability.STRENGTH, Ability.DEXTERITY), List.copyOf(parsed.getSavingThrows()));
    }
}
