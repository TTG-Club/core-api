package club.ttg.dnd5.config;

import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.character_class.rest.dto.PrimaryAbilitiesDto;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

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

    /**
     * Тело запроса. Порядок теряется именно здесь: PUT приносил основные
     * характеристики и категории владений в одном порядке, а сохранялись они в
     * другом, потому что веб-маппер собирал их в HashSet.
     */
    @Test
    void requestBodyKeepsSentOrder() throws Exception
    {
        ObjectMapper web = webMapper();

        PrimaryAbilitiesDto straight = web.readValue(
                "{\"values\":[\"DEXTERITY\",\"WISDOM\"]}", PrimaryAbilitiesDto.class);
        PrimaryAbilitiesDto reversed = web.readValue(
                "{\"values\":[\"WISDOM\",\"DEXTERITY\"]}", PrimaryAbilitiesDto.class);

        assertEquals(List.of(Ability.DEXTERITY, Ability.WISDOM), List.copyOf(straight.getValues()));
        assertEquals(List.of(Ability.WISDOM, Ability.DEXTERITY), List.copyOf(reversed.getValues()));
    }

    /** Спасброски класса тем же путём: тело запроса → DTO. */
    @Test
    void requestSavingThrowsKeepSentOrder() throws Exception
    {
        ClassRequest parsed = webMapper().readValue(
                "{\"savingThrows\":[\"STRENGTH\",\"DEXTERITY\"]}", ClassRequest.class);

        assertEquals(List.of(Ability.STRENGTH, Ability.DEXTERITY), List.copyOf(parsed.getSavingThrows()));
    }

    /** Веб-маппер так же, как его собирает {@link FilterSubtypeAutoConfig}. */
    private ObjectMapper webMapper()
    {
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();

        new FilterSubtypeAutoConfig().keepSetOrder().customize(builder);

        return builder.build();
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
