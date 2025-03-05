package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.dictionary.character.SpellcasterType;
import club.ttg.dnd5.domain.beastiary.model.BeastType;
import club.ttg.dnd5.domain.beastiary.model.Environment;
import club.ttg.dnd5.domain.common.dictionary.*;
import club.ttg.dnd5.domain.common.rest.dto.select.DiceOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.MeasurableSelectOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.SelectOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.SpellcasterOptionDto;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.spell.model.ComparisonOperator;
import club.ttg.dnd5.domain.spell.model.SpellAreaOfEffect;
import club.ttg.dnd5.domain.spell.model.enums.CastingUnit;
import club.ttg.dnd5.domain.spell.model.enums.DistanceUnit;
import club.ttg.dnd5.domain.spell.model.enums.DurationUnit;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
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
        return Arrays.stream(BeastType.values())
                .map(type -> createBaseOptionDTO(type.getName(), type.name()))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getCreatureSizes() {
        return Arrays.stream(Size.values())
                .map(size -> createBaseOptionDTO(size.getName(), size.name()))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getDamageTypes() {
        return Arrays.stream(DamageType.values())
                .map(type -> createBaseOptionDTO(type.getCyrillicName(), type.name()))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getConditions() {
        return Arrays.stream(Condition.values())
                .map(type -> createBaseOptionDTO(type.getCyrillicName(), type.name()))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getAlignments() {
        return Arrays.stream(Alignment.values())
                .map(type -> createBaseOptionDTO(type.getName(), type.name()))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getEnvironments() {
        return Arrays.stream(Environment.values())
                .map(type -> createBaseOptionDTO(type.getName(), type.name()))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getFeatTypes() {
        return Arrays.stream(FeatCategory.values())
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
}
