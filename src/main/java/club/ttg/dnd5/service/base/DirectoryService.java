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
import club.ttg.dnd5.dto.select.DiceSelectOptionDto;
import club.ttg.dnd5.dto.select.SelectOptionDto;
import club.ttg.dnd5.dto.select.SpellcasterSelectOptionDto;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class DirectoryService {
    private SelectOptionDto createSelectOptionDTO(String label, String value) {
        return SelectOptionDto.builder()
                .label(label)
                .value(value)
                .build();
    }

    public Collection<DiceSelectOptionDto> getDices() {
        return Arrays.stream(Dice.values())
                .map(type -> DiceSelectOptionDto.builder()
                        .label(type.getName())
                        .value(type.name())
                        .maxValue(type.getMaxValue())
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getCreatureCategories() {
        return Arrays.stream(CreatureType.values())
                .map(type -> createSelectOptionDTO(type.getCyrillicName(), type.name()))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getCreatureSizes() {
        return Arrays.stream(Size.values())
                .map(size -> createSelectOptionDTO(size.getName(), size.name()))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getDamageTypes() {
        return Arrays.stream(DamageType.values())
                .map(type -> createSelectOptionDTO(type.getCyrillicName(), type.name()))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getConditions() {
        return Arrays.stream(Condition.values())
                .map(type -> createSelectOptionDTO(type.getCyrillicName(), type.name()))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getAlignments() {
        return Arrays.stream(Alignment.values())
                .map(type -> createSelectOptionDTO(type.getName(), type.name()))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getEnvironments() {
        return Arrays.stream(Environment.values())
                .map(type -> createSelectOptionDTO(type.getName(), type.name()))
                .collect(Collectors.toList());
    }

    public Collection<SelectOptionDto> getFeatTypes() {
        return Arrays.stream(FeatCategory.values())
                .map(type -> createSelectOptionDTO(type.getName(), type.name()))
                .collect(Collectors.toList());
    }

    public Collection<SpellcasterSelectOptionDto> getSpellcasterTypes() {
        return Arrays.stream(SpellcasterType.values())
                .map(type -> SpellcasterSelectOptionDto.builder()
                        .label(type.getName())
                        .value(type.name())
                        .levels(type.getMaxSpellLevel())
                        .build())
                .collect(Collectors.toList());
    }
}
