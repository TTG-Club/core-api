package club.ttg.dnd5.domain.feat.rest.mapper;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.model.AbilityBonus;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.mechanics.ChoiceOption;
import club.ttg.dnd5.domain.feat.model.mechanics.ChoiceType;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatChoice;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.feat.model.mechanics.SpellFilter;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@ExtendWith(MockitoExtension.class)
class FeatMechanicsMappingTest {

    @Mock
    private BaseMapping baseMapping;

    @InjectMocks
    private FeatMapperImpl mapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void choiceCountDefaultsToOne() {
        FeatChoice choice = new FeatChoice();

        assertEquals(1, choice.resolveCount());

        choice.setCount(0);
        assertEquals(1, choice.resolveCount());

        choice.setCount(3);
        assertEquals(3, choice.resolveCount());
    }

    @Test
    void rawFormKeepsChoicesOfEntity() {
        Feat feat = new Feat();
        feat.setMechanics(shadowTouched());

        FeatRequest request = mapper.toRequest(feat);
        FeatChoice choice = request.getMechanics().getChoices().getFirst();

        assertEquals("spell", choice.getKey());
        assertEquals(ChoiceType.SPELL, choice.getType());
        assertEquals(1, choice.getSpellFilter().getLevel());
        assertEquals(Set.of(MagicSchool.ILLUSION, MagicSchool.NECROMANCY), choice.getSpellFilter().getSchools());
    }

    @Test
    void abilityBonusCanFollowEarlierChoice() {
        FeatMechanics mechanics = new FeatMechanics();
        FeatChoice save = new FeatChoice();
        save.setKey("saving-throw");
        save.setType(ChoiceType.SAVING_THROW);
        save.setOnlyIfNotProficient(true);
        mechanics.setChoices(List.of(save));

        AbilityBonus bonus = new AbilityBonus();
        bonus.setBonus(1);
        bonus.setUpto(20);
        bonus.setFromChoiceKey("saving-throw");
        mechanics.setAbilityBonuses(List.of(bonus));

        Feat feat = new Feat();
        feat.setMechanics(mechanics);
        feat.setAbilities(List.of(Ability.CONSTITUTION));

        // Повышение привязано к выбору, своего списка характеристик у него нет — значит
        // заменить денормализованное abilities нечем, и черта остаётся в фильтре.
        mapper.syncAbilitiesWithMechanics(feat);

        assertEquals("saving-throw", feat.getMechanics().getAbilityBonuses().getFirst().getFromChoiceKey());
        assertIterableEquals(List.of(Ability.CONSTITUTION), feat.getAbilities());
    }

    @Test
    void emptyFlagsAreNotSerialized() throws Exception {
        FeatChoice choice = new FeatChoice();
        choice.setKey("damage-type");
        choice.setType(ChoiceType.DAMAGE_TYPE);
        choice.setOptions(List.of(new ChoiceOption("FIRE", "Огненный")));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(choice));

        assertEquals("DAMAGE_TYPE", json.get("type").asText());
        assertEquals("FIRE", json.get("options").get(0).get("value").asText());
        assertFalse(json.has("rechooseOnLongRest"));
        assertFalse(json.has("spellFilter"));
        assertFalse(json.has("count"));
    }

    private static FeatMechanics shadowTouched() {
        SpellFilter filter = new SpellFilter();
        filter.setLevel(1);
        filter.setSchools(Set.of(MagicSchool.ILLUSION, MagicSchool.NECROMANCY));

        FeatChoice spell = new FeatChoice();
        spell.setKey("spell");
        spell.setType(ChoiceType.SPELL);
        spell.setLabel("Заклинание 1 уровня школы иллюзии или некромантии");
        spell.setSpellFilter(filter);

        AbilityBonus bonus = new AbilityBonus();
        bonus.setAbilities(List.of(Ability.INTELLIGENCE, Ability.WISDOM, Ability.CHARISMA));
        bonus.setBonus(1);
        bonus.setUpto(20);

        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setAbilityBonuses(List.of(bonus));
        mechanics.setChoices(List.of(spell));
        return mechanics;
    }
}
