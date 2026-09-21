package club.ttg.dnd5.config;

import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.character_class.rest.dto.PrimaryAbilitiesDto;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.spell.model.Spell;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hypersistence.utils.hibernate.type.util.JsonConfiguration;
import io.hypersistence.utils.hibernate.type.util.ObjectMapperWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    /**
     * Незнакомые ключи эффекта переживают колонку jsonb, а не только круг через
     * ObjectMapper: тот же путь, что у {@code @Type(JsonType.class)} — обёртка из
     * {@code hypersistence-utils.properties}, тип поля сущности со всеми
     * параметрами. Копия — это снимок, с которым Hibernate сверяет сущность при
     * сбросе: потеряй она ключ, правка без него выглядела бы как «ничего не
     * изменилось».
     */
    @Test
    void activeEffectUnknownKeysSurviveJsonbColumn() throws Exception
    {
        ObjectMapperWrapper jsonb = JsonConfiguration.INSTANCE.getObjectMapperWrapper();
        Type column = Spell.class.getDeclaredField("activeEffects").getGenericType();
        String stored = "[{\"id\":\"effect-future\","
                + "\"changes\":[{\"key\":\"armorClass\",\"futureScale\":{\"per\":\"level\"}}],"
                + "\"aura\":{\"radius\":10,\"futurePulse\":true},"
                + "\"applySave\":{\"ability\":\"wisdom\",\"futureRetry\":[1,2]},"
                + "\"futureField\":\"как есть\"}]";

        // Обёртка собрана нашим поставщиком, а не маппером Hypersistence по умолчанию
        assertTrue(jsonb.getObjectMapper().getRegisteredModuleIds().contains("ordered-sets"));

        List<ActiveEffect> read = jsonb.fromString(stored, column);

        assertEquals(stored, jsonb.toString(read));
        assertEquals(stored, jsonb.toString(jsonb.clone(read)));
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
