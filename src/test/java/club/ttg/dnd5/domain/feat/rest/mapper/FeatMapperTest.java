package club.ttg.dnd5.domain.feat.rest.mapper;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.model.AbilityBonus;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FeatMapperTest {

    @Mock
    private BaseMapping baseMapping;

    @InjectMocks
    private FeatMapperImpl mapper;

    @Test
    void syncCollectsAbilitiesFromAllVariants() {
        Feat feat = featWithBonuses(
                bonus(2, 20, 1, Ability.STRENGTH, Ability.DEXTERITY),
                bonus(1, 20, 2, Ability.DEXTERITY, Ability.CONSTITUTION));

        mapper.syncAbilitiesWithMechanics(feat);

        assertIterableEquals(
                List.of(Ability.STRENGTH, Ability.DEXTERITY, Ability.CONSTITUTION),
                feat.getAbilities());
    }

    @Test
    void syncKeepsLegacyAbilitiesWhenMechanicsIsEmpty() {
        Feat feat = new Feat();
        feat.setAbilities(List.of(Ability.WISDOM));
        feat.setMechanics(new FeatMechanics());

        mapper.syncAbilitiesWithMechanics(feat);

        assertIterableEquals(List.of(Ability.WISDOM), feat.getAbilities());
    }

    @Test
    void abilityScoreIncreaseOptionsTakesLargestVariantRegardlessOfName() {
        Feat feat = featWithBonuses(
                bonus(2, 20, 1, Ability.STRENGTH),
                bonus(1, 20, 2, Ability.STRENGTH));
        feat.setName("Переименованная черта");

        assertEquals(2, mapper.getAbilityScoreIncreaseOptions(feat));
    }

    @Test
    void abilityScoreIncreaseOptionsDefaultsToOneWhenCountIsAbsent() {
        Feat feat = featWithBonuses(bonus(1, 30, null, Ability.CHARISMA));

        assertEquals(1, mapper.getAbilityScoreIncreaseOptions(feat));
    }

    @Test
    void abilityScoreIncreaseOptionsFallsBackToLegacyAbilities() {
        Feat feat = new Feat();
        feat.setName("Черта без механики");
        feat.setAbilities(List.of(Ability.INTELLIGENCE));

        assertEquals(1, mapper.getAbilityScoreIncreaseOptions(feat));
    }

    @Test
    void abilityScoreIncreaseOptionsIsZeroWithoutAnyImprovement() {
        Feat feat = new Feat();
        feat.setName("Черта без повышения");

        assertEquals(0, mapper.getAbilityScoreIncreaseOptions(feat));
    }

    /**
     * Активные эффекты доезжают до записи и возвращаются в форму: {@code /raw} и снимки
     * ревизий собираются тем же {@code toRequest}, поэтому потеря поля здесь означала бы
     * и пустую вкладку «Эффекты» в редакторе.
     */
    @Test
    void keepsActiveEffectsBetweenRequestAndEntity() {
        FeatRequest request = new FeatRequest();
        request.setActiveEffects(List.of(effect("feat-tough-hp")));

        Feat feat = mapper.toEntity(request, null);

        assertNotNull(feat.getActiveEffects());
        assertEquals(1, feat.getActiveEffects().size());
        assertEquals("feat-tough-hp", feat.getActiveEffects().getFirst().getId());
        assertEquals("feat-tough-hp", mapper.toRequest(feat).getActiveEffects().getFirst().getId());
    }

    /**
     * Пустой список стирает прежние эффекты: мастерская шлёт механику и эффекты целиком,
     * и удалённый в форме эффект обязан исчезнуть из записи, а не пережить сохранение.
     */
    @Test
    void emptyActiveEffectsClearStoredOnes() {
        Feat feat = new Feat();
        feat.setActiveEffects(new java.util.ArrayList<>(List.of(effect("feat-tough-hp"))));

        FeatRequest request = new FeatRequest();
        request.setActiveEffects(List.of());

        mapper.updateEntity(request, null, feat);

        assertTrue(feat.getActiveEffects().isEmpty());
    }

    private static ActiveEffect effect(String id) {
        ActiveEffect effect = new ActiveEffect();
        effect.setId(id);
        effect.setName("Крепкий");
        return effect;
    }

    private static Feat featWithBonuses(AbilityBonus... bonuses) {
        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setAbilityBonuses(List.of(bonuses));
        Feat feat = new Feat();
        feat.setMechanics(mechanics);
        return feat;
    }

    private static AbilityBonus bonus(Integer value, Integer upto, Integer count, Ability... abilities) {
        AbilityBonus bonus = new AbilityBonus();
        bonus.setAbilities(List.of(abilities));
        bonus.setBonus(value);
        bonus.setUpto(upto);
        bonus.setCount(count);
        return bonus;
    }
}
