package club.ttg.dnd5.domain.character_class.rest.mapper;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.character_class.model.*;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassDetailedResponse;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassFeatureDto;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassShortResponse;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.rest.dto.select.DiceOptionDto;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.*;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {BaseMapping.class}, componentModel = "spring")
public interface ClassMapper {

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "hasSubclasses", source = "subclasses", qualifiedByName = "hasSubclasses")
    ClassShortResponse toShortResponse(CharacterClass characterClass);

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "userId", source = "username")
    @Mapping(target = "gallery", ignore = true)
    @Mapping(target = "features", source = ".")
    @Mapping(target = "proficiency.armor", source = "armorProficiency", qualifiedByName = "armorProficiencyToString")
    @Mapping(target = "proficiency.weapon", source = "weaponProficiency", qualifiedByName = "weaponProficiencyToString")
    @Mapping(target = "proficiency.tool", source = "toolProficiency")
    @Mapping(target = "proficiency.skill", source = "skillProficiency", qualifiedByName = "skillProficiencyToString")
    @Mapping(target = "savingThrows", source = "savingThrows", qualifiedByName = "toSavingThrowsString")
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
    @Mapping(target = "proficiency.armor", source = "armorProficiency")
    @Mapping(target = "proficiency.weapon", source = "weaponProficiency")
    @Mapping(target = "proficiency.tool", source = "toolProficiency")
    @Mapping(target = "proficiency.skill", source = "skillProficiency")
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
    @Mapping(target = "armorProficiency", source = "request.proficiency.armor")
    @Mapping(target = "weaponProficiency", source = "request.proficiency.weapon")
    @Mapping(target = "toolProficiency", source = "request.proficiency.tool")
    @Mapping(target = "skillProficiency", source = "request.proficiency.skill")
    @Mapping(target = "equipment", source = "request.equipment")
    @Mapping(target = "casterType", source = "request.casterType")
    @interface ToEntityMapping {
    }

    @Named("toSavingThrowsString")
    default String toSavingThrowsString(Collection<Ability> savingThrows) {
        return savingThrows.stream().map(Ability::getName).collect(Collectors.joining(", "));
    }

    default DiceOptionDto toDiceOptionDto(Dice dice) {
        return DiceOptionDto.builder()
                .label(dice.getName())
                .value(dice.name())
                .maxValue(dice.getMaxValue())
                .avg(dice.getAvgValue())
                .build();
    }

    @Named("armorProficiencyToString")
    default String armorProficiencyToString(ArmorProficiency proficiency) {
        return proficiency == null ? "" : proficiency.toString();
    }

    @Named("weaponProficiencyToString")
    default String weaponProficiencyToString(WeaponProficiency proficiency) {
        return proficiency == null ? "" : proficiency.toString();
    }

    @Named("skillProficiencyToString")
    default String skillProficiencyToString(SkillProficiency proficiency) {
        return proficiency == null ? "" : proficiency.toString();
    }

    default List<ClassFeatureDto> toFeaturesDto(CharacterClass characterClass) {
        boolean isSubclass = Objects.isNull(characterClass.getParent());
        List<ClassFeatureDto> parentFeaturesDtos = Optional.ofNullable(characterClass.getParent())
                .map(CharacterClass::getFeatures)
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

    @Named("hasSubclasses")
    default boolean hasSubclasses(Collection<CharacterClass> subclasses) {
        return !CollectionUtils.isEmpty(subclasses);
    }
}
