package club.ttg.dnd5.service.base;

import club.ttg.dnd5.dictionary.Alignment;
import club.ttg.dnd5.dictionary.DamageType;
import club.ttg.dnd5.dictionary.Dice;
import club.ttg.dnd5.dictionary.Size;
import club.ttg.dnd5.dictionary.beastiary.Condition;
import club.ttg.dnd5.dictionary.beastiary.CreatureType;
import club.ttg.dnd5.dictionary.beastiary.Environment;
import club.ttg.dnd5.dictionary.character.FeatCategory;
import club.ttg.dnd5.dictionary.character.SpellcasterType;
import club.ttg.dnd5.dto.select.DiceOptionDto;
import club.ttg.dnd5.dto.select.SelectOptionDto;
import club.ttg.dnd5.dto.select.SpellcasterOptionDto;
import club.ttg.dnd5.model.spell.enums.ComparisonOperator;
import club.ttg.dnd5.model.spell.enums.MagicSchool;
import club.ttg.dnd5.model.spell.enums.TimeUnit;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DictionariesService {
    public List<SelectOptionDto> getTimeUnits() {
        return Arrays.stream(TimeUnit.values())
                .map(unit -> SelectOptionDto.builder()
                        .label(unit.getName())
                        .value(unit.name())
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
                .map(type -> createBaseOptionDTO(type.getCyrillicName(), type.name()))
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
}
