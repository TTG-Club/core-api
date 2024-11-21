package club.ttg.dnd5.controller.tools.dictionary;

import club.ttg.dnd5.dictionary.Alignment;
import club.ttg.dnd5.dictionary.DamageType;
import club.ttg.dnd5.dictionary.Dice;
import club.ttg.dnd5.dictionary.Size;
import club.ttg.dnd5.dictionary.beastiary.Condition;
import club.ttg.dnd5.dictionary.beastiary.CreatureType;
import club.ttg.dnd5.dictionary.beastiary.Environment;
import club.ttg.dnd5.dictionary.character.FeatCategory;
import club.ttg.dnd5.dictionary.character.SpellcasterType;
import club.ttg.dnd5.dto.NameDto;
import club.ttg.dnd5.dto.ValueDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Различные справочники
 */
@RequiredArgsConstructor
@Tag(name = "Справочники", description = "API для различных справочников")
@RequestMapping("/api/v2/directory/")
@RestController
public class DirectoryController {
    @Operation(summary = "Дайсы")
    @GetMapping("/dices")
    public Collection<ValueDto> getDices() {
        return Arrays.stream(Dice.values())
                .map(type -> ValueDto.builder()
                    .rus(type.getName())
                    .eng(type.name())
                    .value(type.getMaxValue())
                    .build())
                .collect(Collectors.toList()
        );
    }

    @Operation(summary = "Типы существ")
    @GetMapping("/beast/types")
    public Collection<NameDto> getCreatureCategory() {
        return Arrays.stream(CreatureType.values())
                .map(type -> NameDto.builder()
                    .rus(type.getCyrillicName())
                    .eng(type.name())
                    .build()
                )
                .collect(Collectors.toList()
        );
    }

    @Operation(summary = "Размеры существ")
    @GetMapping("/sizes")
    public Collection<NameDto> getCreatureSize() {
        return Arrays.stream(Size.values())
                .map(size -> NameDto.builder()
                        .rus(size.getName())
                        .eng(size.name())
                        .build()
                )
                .collect(Collectors.toList()
        );
    }

    @Operation(summary = "Типы урона")
    @GetMapping("/damage/types")
    public Collection<NameDto> getDamageType() {
        return Arrays.stream(DamageType.values())
                .map(type -> NameDto.builder()
                    .rus(type.getCyrillicName())
                    .eng(type.name())
                        .build())
                .collect(Collectors.toList()
        );
    }

    @Operation(summary = "Состояния")
    @GetMapping("/conditions")
    public Collection<NameDto> getConditions() {
        return Arrays.stream(Condition.values())
                .map(type -> NameDto.builder()
                    .rus(type.getCyrillicName())
                    .eng(type.name())
                    .build())
                .collect(Collectors.toList()
        );
    }

    @Operation(summary = "Мировоззрение")
    @GetMapping("/alignments")
    public Collection<NameDto> getAlignments() {
        return Arrays.stream(Alignment.values())
                .map(type -> NameDto.builder()
                    .rus(type.getName())
                    .eng(type.name())
                    .build())
                .collect(Collectors.toList()
        );
    }

    @Operation(summary = "Места обитания существ")
    @GetMapping("/environments")
    public Collection<NameDto> getEnvironments() {
        return Arrays.stream(Environment.values())
                .map(type -> NameDto.builder()
                    .rus(type.getName())
                    .eng(type.name())
                    .build())
                .collect(Collectors.toList()
        );
    }

    @Operation(summary = "Типы черт")
    @GetMapping("/type_feats")
    public Collection<NameDto> getFeatTypes() {
        return Arrays.stream(FeatCategory.values())
                .map(type -> NameDto.builder()
                        .rus(type.getName())
                        .eng(type.name())
                        .build())
                .collect(Collectors.toList()
                );
    }

    @Operation(summary = "Типы заклинателей")
    @GetMapping("/spellcaster_types")
    public Collection<ValueDto> getSpellcasterTypes() {
        return Arrays.stream(SpellcasterType.values())
                .map(type -> ValueDto.builder()
                        .rus(type.getName())
                        .eng(type.name())
                        .value(type.getMaxSpellLevel())
                        .build())
                .collect(Collectors.toList()
                );
    }

    @Operation(summary = "Типы черт")
    @GetMapping("/feat_types")
    public Collection<NameDto> getFeatTypesSpellcasterTypes() {
        return Arrays.stream(FeatCategory.values())
                .map(type -> NameDto.builder()
                        .rus(type.getName())
                        .eng(type.name())
                        .build())
                .collect(Collectors.toList()
                );
    }
}
