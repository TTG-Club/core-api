package club.ttg.dnd5.domain.character_class.rest.mapper;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.character_class.model.ArmorProficiency;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.ClassFeature;
import club.ttg.dnd5.domain.character_class.model.SkillProficiency;
import club.ttg.dnd5.domain.character_class.model.WeaponProficiency;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassDetailedResponse;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassFeatureDto;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassShortResponse;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.rest.dto.select.DiceOptionDto;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {BaseMapping.class}, componentModel = "spring")
public interface ClassMapper {

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "userId", source = "username")
    @Mapping(target = "gallery", ignore = true)
    ClassShortResponse toShortResponse(CharacterClass characterClass);

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "userId", source = "username")
    @Mapping(target = "gallery", ignore = true)
    @Mapping(target = "features", source = ".")
    ClassDetailedResponse toDetailedResponse(CharacterClass characterClass);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(target = "parent", source = "parent")
    @Mapping(target = "subclasses", ignore = true)

    @ToEntityMapping
    CharacterClass toEntity(ClassRequest request, CharacterClass parent, Book source);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(target = "parent", source = "parent")
    @Mapping(target = "subclasses", ignore = true)

    @ToEntityMapping
    CharacterClass updateEntity(@MappingTarget CharacterClass existingClass, CharacterClass parent, ClassRequest request, Book source);

    @BaseMapping.BaseRequestNameMapping
    @Mapping(target = "gallery", ignore = true)
    @Mapping(target = "parentUrl", source = "parent.url")
    @Mapping(source = "source.url", target = "source.url")
    @Mapping(source = "sourcePage", target = "source.page")
    ClassRequest toRequest(CharacterClass entity);

    @Mapping(target = "url", source = "request.url")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "imageUrl", source = "request.imageUrl")
    @Mapping(target = "hitDice", source = "request.hitDice")
    @Mapping(target = "savingThrows", source = "request.savingThrows")
    @Mapping(target = "features", source = "request.features")
    @Mapping(target = "table", source = "request.table")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "sourcePage", source = "request.source.page")
    @Mapping(target = "armorProficiency", source = "request.armorProficiency")
    @Mapping(target = "weaponProficiency", source = "request.weaponProficiency")
    @Mapping(target = "toolProficiency", source = "request.toolProficiency")
    @Mapping(target = "skillProficiency", source = "request.skillProficiency")
    @Mapping(target = "equipment", source = "request.equipment")
    @Mapping(target = "casterType", source = "request.casterType")
    @interface ToEntityMapping {
    }

    default String toSavingThrowsString(Collection<Ability> savingThrows) {
        return savingThrows.stream().map(Ability::toString).collect(Collectors.joining(","));
    }

    default DiceOptionDto toDiceOptionDto(Dice dice) {
        return DiceOptionDto.builder()
                .label(dice.getName())
                .value(dice.name())
                .maxValue(dice.getMaxValue())
                .build();
    }

    default String armorProficiencyToString(ArmorProficiency proficiency) {
        return proficiency == null ? "" : proficiency.toString();
    }

    default String weaponProficiencyToString(WeaponProficiency proficiency) {
        return proficiency == null ? "" : proficiency.toString();
    }

    default String skillProficiencyToString(SkillProficiency proficiency) {
        return proficiency == null ? "" : proficiency.toString();
    }

    default List<ClassFeatureDto> toFeaturesDto(CharacterClass characterClass) {
        boolean isSubclass = Objects.isNull(characterClass.getParent());
        List<ClassFeatureDto> parentFeaturesDtos = Optional.ofNullable(characterClass.getParent().getFeatures())
                .orElse(List.of()).stream()
                .map(f -> new ClassFeatureDto(f, false))
                .collect(Collectors.toList());
        List<ClassFeatureDto> classFeatureDtos = characterClass.getFeatures().stream()
                .map(f -> new ClassFeatureDto(f, isSubclass))
                .collect(Collectors.toList());
        return Stream.of(parentFeaturesDtos, classFeatureDtos)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(ClassFeature::getLevel))
                .collect(Collectors.toList());
    }
}
