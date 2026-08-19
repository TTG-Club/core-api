package club.ttg.dnd5.domain.feat.rest.mapper;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.model.AbilityBonus;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceGrant;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceOption;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceType;
import club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.common.model.mechanics.SpellFilter;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FeatMechanicsMappingTest {

    @Mock
    private BaseMapping baseMapping;

    @InjectMocks
    private FeatMapperImpl mapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void choiceCountDefaultsToOne() {
        MechanicChoice choice = new MechanicChoice();

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
        MechanicChoice choice = request.getMechanics().getChoices().getFirst();

        assertEquals("spell", choice.getKey());
        assertEquals(ChoiceType.SPELL, choice.getType());
        assertEquals(1, choice.getSpellFilter().getLevel());
        assertEquals(Set.of(MagicSchool.ILLUSION, MagicSchool.NECROMANCY), choice.getSpellFilter().getSchools());
    }

    @Test
    void abilityBonusCanFollowEarlierChoice() {
        FeatMechanics mechanics = new FeatMechanics();
        MechanicChoice save = new MechanicChoice();
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

    /**
     * «Знаток»: выбрать можно только навык, которым персонаж уже владеет, и выбор сразу
     * поднимает его до компетентности.
     */
    @Test
    void choiceCanGrantExpertiseFromOwnedSkills() throws Exception {
        MechanicChoice choice = new MechanicChoice();
        choice.setKey("skill");
        choice.setType(ChoiceType.SKILL);
        choice.setOnlyIfProficient(true);
        choice.setGrants(ChoiceGrant.EXPERTISE);

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(choice));

        assertEquals("EXPERTISE", json.get("grants").asText());
        assertTrue(json.get("onlyIfProficient").asBoolean());
        assertEquals(ChoiceGrant.EXPERTISE, choice.resolveGrant());
    }

    /** Записи до появления исхода давали владение — им он и подставляется. */
    @Test
    void choiceGrantDefaultsToProficiency() throws Exception {
        MechanicChoice choice = new MechanicChoice();
        choice.setKey("skill");
        choice.setType(ChoiceType.SKILL);

        assertEquals(ChoiceGrant.PROFICIENCY, choice.resolveGrant());

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(choice));

        assertFalse(json.has("grants"));
        assertFalse(json.has("onlyIfProficient"));
    }

    @Test
    void emptyFlagsAreNotSerialized() throws Exception {
        MechanicChoice choice = new MechanicChoice();
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

    /**
     * «Посвящённый в магию»: класс не задан заранее — игрок выбирает список жреца, друида
     * или волшебника, и пул заговоров сужается до выбранного, а не до всех трёх сразу.
     */
    @Test
    void spellFilterCanFollowChosenClassList() {
        Feat feat = new Feat();
        feat.setMechanics(magicInitiate());

        FeatRequest request = mapper.toRequest(feat);
        SpellFilter filter = request.getMechanics().getChoices().getLast().getSpellFilter();

        assertEquals("spell-list", filter.getClassesFromChoiceKey());
        // Список задан ответом игрока, поэтому своего перечня классов у фильтра нет.
        assertNull(filter.getClasses());
    }

    /** Незаполненная ссылка на выбор в JSON не уходит: пул тогда задан классами. */
    @Test
    void emptyClassChoiceKeyIsOmitted() throws Exception {
        SpellFilter filter = new SpellFilter();
        filter.setLevel(0);

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(filter));

        assertFalse(json.has("classesFromChoiceKey"));
        assertFalse(json.has("classes"));
    }

    private static FeatMechanics magicInitiate() {
        MechanicChoice list = new MechanicChoice();
        list.setKey("spell-list");
        list.setType(ChoiceType.SPELL_LIST);
        list.setLabel("Список заклинаний: жрец, друид или волшебник");

        SpellFilter filter = new SpellFilter();
        filter.setLevel(0);
        filter.setClassesFromChoiceKey("spell-list");

        MechanicChoice cantrip = new MechanicChoice();
        cantrip.setKey("cantrip");
        cantrip.setType(ChoiceType.CANTRIP);
        cantrip.setCount(2);
        cantrip.setLabel("Два заговора из выбранного списка");
        cantrip.setSpellFilter(filter);

        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setChoices(List.of(list, cantrip));
        return mechanics;
    }

    private static FeatMechanics shadowTouched() {
        SpellFilter filter = new SpellFilter();
        filter.setLevel(1);
        filter.setSchools(Set.of(MagicSchool.ILLUSION, MagicSchool.NECROMANCY));

        MechanicChoice spell = new MechanicChoice();
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
