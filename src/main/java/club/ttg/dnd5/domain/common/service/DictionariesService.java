package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.common.dictionary.Language;
import club.ttg.dnd5.domain.common.dictionary.SpellcasterType;
import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.common.dictionary.SenseType;
import club.ttg.dnd5.domain.common.dictionary.*;
import club.ttg.dnd5.domain.common.rest.dto.select.BaseSelectOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.CrlOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.DiceOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.KeySelectDto;
import club.ttg.dnd5.domain.common.rest.dto.select.MeasurableSelectOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.SelectOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.SkillOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.SpellcasterOptionDto;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import club.ttg.dnd5.domain.spell.model.ComparisonOperator;
import club.ttg.dnd5.domain.spell.model.SpellAreaOfEffect;
import club.ttg.dnd5.domain.spell.model.enums.CastingUnit;
import club.ttg.dnd5.domain.spell.model.enums.DistanceUnit;
import club.ttg.dnd5.domain.spell.model.enums.DurationUnit;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DictionariesService {
    public List<MeasurableSelectOptionDto> getTimeUnits() {
        return Arrays.stream(CastingUnit.values())
                .map(unit -> MeasurableSelectOptionDto.builder()
                        .label(unit.getName())
                        .value(unit.name())
                        .measurable(unit.getMeasurable())
                        .build())
                .collect(Collectors.toList());
    }

    public List<MeasurableSelectOptionDto> getSpellDurationUnits() {
        return Arrays.stream(DurationUnit.values())
                .map(unit -> MeasurableSelectOptionDto.builder()
                        .label(unit.getName())
                        .value(unit.name())
                        .measurable(unit.getMeasurable())
                        .build())
                .collect(Collectors.toList());
    }

    public List<SelectOptionDto> getMagicSchools() {
        return Arrays.stream(MagicSchool.values())
                .map(school -> SelectOptionDto.builder()
                        .label(school.getName())
                        .value(school.name())
                        .build())
                .sorted(Comparator.comparing(SelectOptionDto::getLabel))
                .collect(Collectors.toList());
    }

    public Collection<MeasurableSelectOptionDto> getSpellDistanceUnits() {
        return Arrays.stream(DistanceUnit.values())
                .map(unit -> MeasurableSelectOptionDto.builder()
                        .label(unit.getName())
                        .value(unit.name())
                        .measurable(unit.getMeasurable())
                        .build())
                .collect(Collectors.toList());
    }

    public List<SelectOptionDto> getComparisonOperators() {
        return Arrays.stream(ComparisonOperator.values())
                .map(op -> SelectOptionDto.builder()
                        .label(op.getSymbol())
                        .value(op.name())
                        .build())
                .collect(Collectors.toList());
    }

    private SelectOptionDto createBaseOptionDTO(String label, String value) {
        return SelectOptionDto.builder()
                .label(label)
                .value(value)
                .build();
    }

    public Collection<DiceOptionDto> getDices() {
        return Arrays.stream(Dice.values())
                .map(type -> DiceOptionDto.builder()
                        .label(type.getName())
                        .value(type.name())
                        .maxValue(type.getMaxValue())
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getCreatureCategories() {
        return Arrays.stream(CreatureType.values())
                .map(type -> createBaseOptionDTO(type.getName(), type.name()))
                .sorted(Comparator.comparing(SelectOptionDto::getLabel))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getCreatureSizes() {
        return Arrays.stream(Size.values())
                .map(size -> createBaseOptionDTO(size.getName(), size.name()))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getDamageTypes() {
        return Arrays.stream(DamageType.values())
                .map(type -> createBaseOptionDTO(type.getName(), type.name()))
                .sorted(Comparator.comparing(SelectOptionDto::getLabel))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getConditions() {
        return Arrays.stream(Condition.values())
                .map(type -> createBaseOptionDTO(type.getName(), type.name()))
                .sorted(Comparator.comparing(SelectOptionDto::getLabel))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getAlignments() {
        return Arrays.stream(Alignment.values())
                .map(type -> createBaseOptionDTO(type.getName(), type.name()))
                .sorted(Comparator.comparing(SelectOptionDto::getLabel))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getFeatTypes() {
        return Arrays.stream(FeatCategory.values())
                .map(type -> createBaseOptionDTO(type.getName(), type.name()))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getTreasures() {
        return Arrays.stream(CreatureTreasure.values())
                .map(type -> createBaseOptionDTO(type.getName(), type.name()))
                .collect(Collectors.toList());
    }

    public Collection<SpellcasterOptionDto> getSpellcasterTypes() {
        return Arrays.stream(SpellcasterType.values())
                .map(type -> SpellcasterOptionDto.builder()
                        .label(type.getName())
                        .value(type.name())
                        .levels(type.getMaxSpellLevel())
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getSpellAreaOfEffect() {
        return Arrays.stream(SpellAreaOfEffect.values())
                .map(type -> SelectOptionDto.builder()
                        .label(type.getName())
                        .value(type.name())
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<KeySelectDto> getAbilities() {
        return Arrays.stream(Ability.values())
                .map(type -> KeySelectDto.builder()
                        .label(type.getName())
                        .value(type.name())
                        .key(type.getKey())
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<SkillOptionDto> getSkills() {
        return Arrays.stream(Skill.values())
                .map(type -> SkillOptionDto.builder()
                        .label(type.getName())
                        .ability(type.getAbility().name())
                        .value(type.name())
                        .build())
                .sorted(Comparator.comparing(BaseSelectOptionDto::getLabel))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getSenseType() {
        return Arrays.stream(SenseType.values())
                .map(type -> SelectOptionDto.builder()
                        .label(type.getName())
                        .value(type.name())
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getHabitats() {
        return Arrays.stream(Habitat.values())
                .map(type -> SelectOptionDto.builder()
                        .label(type.getName())
                        .value(type.name())
                        .build())
                .sorted(Comparator.comparing(SelectOptionDto::getLabel))
                .collect(Collectors.toList());

    }

    public Collection<SelectOptionDto> getMagicItemCategories() {
        return Arrays.stream(MagicItemCategory.values())
                .map(type -> SelectOptionDto.builder()
                        .label(type.getName())
                        .value(type.name())
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getRarities() {
        return Arrays.stream(Rarity.values())
                .map(type -> SelectOptionDto.builder()
                        .label(type.getName())
                        .value(type.name())
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getHealTypes() {
        return Arrays.stream(HealingType.values())
                .map(type -> SelectOptionDto.builder()
                        .label(type.getName())
                        .value(type.name())
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<CrlOptionDto> getChallengeRailings() {
        return Arrays.stream(ChallengeRating.values())
                .map(type -> CrlOptionDto.builder()
                        .label(type.getName())
                        .value(type.getExperience())
                        .pb(type.getProficiencyBonus())
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getProficiencyBonus() {
        return Arrays.stream(ChallengeRating.values())
                .map(type -> SelectOptionDto.builder()
                        .label(type.getName())
                        .value(type.getProficiencyBonus())
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getSenses() {
        return Arrays.stream(SenseType.values())
                .map(type -> SelectOptionDto.builder()
                        .label(type.getName())
                        .value(type.name())
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getLanguages() {
        return Arrays.stream(Language.values())
                .map(type -> SelectOptionDto.builder()
                        .label(type.getName())
                        .value(type.name())
                        .build())
                .sorted(Comparator.comparing(SelectOptionDto::getLabel))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getCoins() {
        return Arrays.stream(Coin.values())
                .map(type -> SelectOptionDto.builder()
                        .label(type.getShortName())
                        .value(type.name())
                        .build())
                .collect(Collectors.toList());
    }
}
