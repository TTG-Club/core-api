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
import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.base.ValueDto;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class DirectoryService {
    private NameBasedDTO createNameBasedDTO(String rus, String eng) {
        return NameBasedDTO.builder()
                .name(rus)
                .english(eng)
                .build();
    }

    public Collection<ValueDto> getDices() {
        return Arrays.stream(Dice.values())
                .map(type -> ValueDto.builder()
                        .name(createNameBasedDTO(type.getName(), type.name()))
                        .value(type.getMaxValue())
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<ValueDto> getCreatureCategories() {
        return Arrays.stream(CreatureType.values())
                .map(type -> ValueDto.builder()
                        .name(createNameBasedDTO(type.getCyrillicName(), type.name()))
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<ValueDto> getCreatureSizes() {
        return Arrays.stream(Size.values())
                .map(size -> ValueDto.builder()
                        .name(createNameBasedDTO(size.getName(), size.name()))
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<ValueDto> getDamageTypes() {
        return Arrays.stream(DamageType.values())
                .map(type -> ValueDto.builder()
                        .name(createNameBasedDTO(type.getCyrillicName(), type.name()))
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<ValueDto> getConditions() {
        return Arrays.stream(Condition.values())
                .map(type -> ValueDto.builder()
                        .name(createNameBasedDTO(type.getCyrillicName(), type.name()))
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<ValueDto> getAlignments() {
        return Arrays.stream(Alignment.values())
                .map(type -> ValueDto.builder()
                        .name(createNameBasedDTO(type.getName(), type.name()))
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<ValueDto> getEnvironments() {
        return Arrays.stream(Environment.values())
                .map(type -> ValueDto.builder()
                        .name(createNameBasedDTO(type.getName(), type.name()))
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<ValueDto> getFeatTypes() {
        return Arrays.stream(FeatCategory.values())
                .map(type -> ValueDto.builder()
                        .name(createNameBasedDTO(type.getName(), type.name()))
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<ValueDto> getSpellcasterTypes() {
        return Arrays.stream(SpellcasterType.values())
                .map(type -> ValueDto.builder()
                        .name(createNameBasedDTO(type.getName(), type.name()))
                        .value(type.getMaxSpellLevel())
                        .build())
                .collect(Collectors.toList());
    }

    public Collection<ValueDto> getFeatTypesSpellcasterTypes() {
        return Arrays.stream(FeatCategory.values())
                .map(type -> ValueDto.builder()
                        .name(createNameBasedDTO(type.getName(), type.name()))
                        .build())
                .collect(Collectors.toList());
    }
}