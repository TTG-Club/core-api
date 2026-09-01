package club.ttg.dnd5.domain.species.rest.mapper;

import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.SenseType;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceOption;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceType;
import club.ttg.dnd5.domain.common.model.mechanics.DamageAffinity;
import club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice;
import club.ttg.dnd5.domain.common.model.mechanics.ProficiencyGrant;
import club.ttg.dnd5.domain.common.model.mechanics.ResourceCounter;
import club.ttg.dnd5.domain.common.model.mechanics.SenseGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SheetModifiers;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.model.SpeciesFeature;
import club.ttg.dnd5.domain.species.model.mechanics.SpeciesMechanics;
import club.ttg.dnd5.domain.species.rest.dto.FeatureRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesFeatureResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Механика умения вида в форме редактора и в детальнике: маппер не должен терять её по
 * дороге, а пустая механика не должна превращаться в пустой объект в JSON.
 */
@ExtendWith(MockitoExtension.class)
class SpeciesMechanicsMappingTest {

    private final SpeciesFeatureMapper featureMapper = new SpeciesFeatureMapperImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Соседи маппера вида: механику они не трогают, но без них не собрать сам маппер. */
    @Mock
    private BaseMapping baseMapping;

    @Mock
    private SpeciesFeatureMapper injectedFeatureMapper;

    @Mock
    private CreaturePropertiesMapper creaturePropertiesMapper;

    @InjectMocks
    private SpeciesMapperImpl speciesMapper;

    /** Сущность → форма: сопротивление, чувство и уровень доезжают целиком. */
    @Test
    void rawFormKeepsMechanicsOfFeature() {
        SpeciesFeature feature = dwarvenResilience();

        FeatureRequest request = featureMapper.toRequest(feature);

        SheetModifiers modifiers = request.getMechanics().getModifiers();
        assertEquals(Set.of(DamageType.POISON), modifiers.getDamage().getResistances());
        assertEquals(SenseType.DARKVISION, modifiers.getSenses().getFirst().getType());
        assertEquals(120, modifiers.getSenses().getFirst().getRange());
        assertNull(request.getLevel());
    }

    /** Форма → сущность: уровень и выбор навыка сохраняются как есть. */
    @Test
    void entityKeepsChoiceFromForm() {
        FeatureRequest request = new FeatureRequest();
        request.setLevel(3);
        SpeciesMechanics mechanics = new SpeciesMechanics();
        mechanics.setChoices(List.of(skillChoice()));
        request.setMechanics(mechanics);

        SpeciesFeature feature = featureMapper.toEntity(request);

        assertEquals(3, feature.getLevel());
        MechanicChoice choice = feature.getMechanics().getChoices().getFirst();
        assertEquals(ChoiceType.SKILL, choice.getType());
        assertEquals(1, choice.resolveCount());
        assertEquals("PERCEPTION", choice.getOptions().getFirst().getValue());
    }

    /** Детальник вида отдаёт механику умения рядом с описанием. */
    @Test
    void detailResponseCarriesMechanics() {
        List<SpeciesFeatureResponse> responses = featureMapper.toResponses(List.of(dwarvenResilience()));

        SpeciesFeatureResponse response = responses.getFirst();
        assertEquals("Дварфийская стойкость", response.getName().getName());
        assertEquals(Set.of(DamageType.POISON),
                response.getMechanics().getModifiers().getDamage().getResistances());
    }

    /** Умение без механики остаётся текстовым: в JSON нет ни пустого объекта, ни уровня. */
    @Test
    void textOnlyFeatureHasNoMechanicsInJson() throws Exception {
        SpeciesFeature feature = new SpeciesFeature("stonecunning", "Знание камня", "Stonecunning",
                "Чувство вибрации на 10 минут.", null);

        JsonNode json = objectMapper.valueToTree(featureMapper.toRequest(feature));

        assertFalse(json.hasNonNull("mechanics"));
        assertFalse(json.hasNonNull("level"));
    }

    /**
     * Механика самой записи ходит через маппер вида в обе стороны: без неё происхождению,
     * у которого нет умений, эффект приписать некуда.
     */
    @Test
    void speciesMechanicsSurvivesRoundTrip() {
        SpeciesMechanics mechanics = new SpeciesMechanics();
        SheetModifiers modifiers = new SheetModifiers();
        DamageAffinity damage = new DamageAffinity();
        damage.setResistances(Set.of(DamageType.FIRE));
        modifiers.setDamage(damage);
        mechanics.setModifiers(modifiers);

        Species lineage = new Species();
        lineage.setUrl("tiefling-infernal-phb");
        lineage.setName("Инфернальный тифлинг");
        lineage.setMechanics(mechanics);

        SpeciesRequest request = speciesMapper.toRequest(lineage);
        assertEquals(Set.of(DamageType.FIRE),
                request.getMechanics().getModifiers().getDamage().getResistances());

        Species restored = speciesMapper.toEntity(request);
        assertEquals(Set.of(DamageType.FIRE),
                restored.getMechanics().getModifiers().getDamage().getResistances());
    }

    /**
     * Счётчик ресурса умения ходит через маппер формы: «Дыхание дракона» на листе — тот же
     * счётчик, что у черты и умения класса, и без поля в механике вида он бы пропадал.
     */
    @Test
    void featureCounterSurvivesFormMapping() {
        ResourceCounter counter = new ResourceCounter();
        counter.setKey("breath-weapon");
        counter.setName("Дыхание дракона");
        counter.setMax("@prof");
        SpeciesMechanics mechanics = new SpeciesMechanics();
        mechanics.setCounters(List.of(counter));
        SpeciesFeature feature = new SpeciesFeature("breath-weapon", "Дыхание дракона",
                "Breath Weapon", "Выдох стихии.", null);
        feature.setMechanics(mechanics);

        FeatureRequest request = featureMapper.toRequest(feature);
        SpeciesFeature restored = featureMapper.toEntity(request);

        ResourceCounter restoredCounter = restored.getMechanics().getCounters().getFirst();
        assertEquals("breath-weapon", restoredCounter.getKey());
        assertEquals("@prof", restoredCounter.getMax());
    }

    /**
     * Тёмное зрение вида — чувство {@code DARKVISION} в механике умения, а не свойство
     * записи: своего поля у вида нет, и форма получает дальность вместе с остальной
     * механикой умения «Тёмное зрение».
     */
    @Test
    void darkVisionLivesInFeatureSenses() {
        SheetModifiers modifiers = new SheetModifiers();
        modifiers.setSenses(List.of(new SenseGrant(SenseType.DARKVISION, 60)));
        SpeciesMechanics mechanics = new SpeciesMechanics();
        mechanics.setModifiers(modifiers);
        SpeciesFeature feature = new SpeciesFeature("darkvision", "Тёмное зрение", "Darkvision",
                "Тёмное зрение 60 фт.", null);
        feature.setMechanics(mechanics);

        FeatureRequest request = featureMapper.toRequest(feature);

        SenseGrant sense = request.getMechanics().getModifiers().getSenses().getFirst();
        assertEquals(SenseType.DARKVISION, sense.getType());
        assertEquals(60, sense.getRange());
    }

    private SpeciesFeature dwarvenResilience() {
        SheetModifiers modifiers = new SheetModifiers();
        DamageAffinity damage = new DamageAffinity();
        damage.setResistances(Set.of(DamageType.POISON));
        modifiers.setDamage(damage);
        modifiers.setSenses(List.of(new SenseGrant(SenseType.DARKVISION, 120)));

        SpeciesMechanics mechanics = new SpeciesMechanics();
        mechanics.setModifiers(modifiers);

        SpeciesFeature feature = new SpeciesFeature("dwarven-resilience", "Дварфийская стойкость",
                "Dwarven Resilience", "Сопротивление урону ядом.", null);
        feature.setMechanics(mechanics);
        return feature;
    }

    private MechanicChoice skillChoice() {
        MechanicChoice choice = new MechanicChoice();
        choice.setKey("skill");
        choice.setType(ChoiceType.SKILL);
        choice.setOptions(List.of(new ChoiceOption("PERCEPTION", "Внимательность"),
                new ChoiceOption("STEALTH", "Скрытность")));
        return choice;
    }

    /** Владения без выбора кладутся в proficiencies, а не в choices. */
    @Test
    void grantedSkillsGoToProficiencies() {
        ProficiencyGrant grant = new ProficiencyGrant();
        grant.setSkills(Set.of(Skill.PERCEPTION));
        SpeciesMechanics mechanics = new SpeciesMechanics();
        mechanics.setProficiencies(grant);

        FeatureRequest request = new FeatureRequest();
        request.setMechanics(mechanics);

        SpeciesFeature feature = featureMapper.toEntity(request);

        assertEquals(Set.of(Skill.PERCEPTION), feature.getMechanics().getProficiencies().getSkills());
        assertNull(feature.getMechanics().getChoices());
    }
}
