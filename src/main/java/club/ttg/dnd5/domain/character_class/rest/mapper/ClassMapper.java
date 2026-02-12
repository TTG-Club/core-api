package club.ttg.dnd5.domain.character_class.rest.mapper;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.character_class.model.*;
import club.ttg.dnd5.domain.character_class.rest.dto.*;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.rest.dto.select.DiceOptionDto;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {BaseMapping.class}, componentModel = "spring")
public interface ClassMapper {

    @Named("toShortResponse")
    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "hasSubclasses", source = "subclasses", qualifiedByName = "hasSubclasses")
    @Mapping(target = "image", source = ".", qualifiedByName = "toImageUrl")
    ClassShortResponse toShortResponse(CharacterClass characterClass);

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "levels", source = "features", qualifiedByName = "getLevels")
    @Mapping(target = "abilityBonus", source = "features", qualifiedByName = "getAbilityBonus")
    ClassAbilityImprovementResponse toAbilityResponse(CharacterClass characterClass);

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
    @Mapping(target = "primaryCharacteristics", source = "primaryCharacteristics", qualifiedByName = "toPrimaryCharacteristics")
    @Mapping(target = "hasSubclasses", source = "subclasses", qualifiedByName = "hasSubclasses")
    @Mapping(target = "parent", source = "parent", qualifiedByName = "toShortResponse")
    @Mapping(target = "imageUrl", source = ".", qualifiedByName = "toImageUrl")
    ClassDetailedResponse toDetailedResponse(CharacterClass characterClass);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "subclasses", ignore = true)
    @Mapping(target = "hiddenEntity", ignore = true)
    @ToEntityMapping
    CharacterClass toEntity(ClassRequest request, Source source);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "subclasses", ignore = true)
    @Mapping(target = "hiddenEntity", ignore = true)

    @ToEntityMapping
    void updateEntity(@MappingTarget CharacterClass existingClass,
                                ClassRequest request,
                                Source source);

    @BaseMapping.BaseRequestNameMapping
    @Mapping(target = "gallery", ignore = true)
    @Mapping(target = "parentUrl", source = "parent.url")
    @Mapping(source = "source.url", target = "source.url")
    @Mapping(source = "sourcePage", target = "source.page")
    @Mapping(target = "proficiency.armor", source = "armorProficiency")
    @Mapping(target = "proficiency.weapon", source = "weaponProficiency")
    @Mapping(target = "proficiency.tool", source = "toolProficiency")
    @Mapping(target = "proficiency.skill", source = "skillProficiency")
    @Mapping(target = "multiclassProficiency", source = "multiclassProficiency")
    ClassRequest toRequest(CharacterClass entity);

    @Mapping(target = "url", source = "request.url")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "imageUrl", source = "request.imageUrl")
    @Mapping(target = "hitDice", source = "request.hitDice")
    @Mapping(target = "savingThrows", source = "request.savingThrows")
    @Mapping(target = "features", source = "request.features", qualifiedByName = "toFeatureEntities")
    @Mapping(target = "table", source = "request.table")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "sourcePage", source = "request.source.page")
    @Mapping(target = "armorProficiency", source = "request.proficiency.armor")
    @Mapping(target = "weaponProficiency", source = "request.proficiency.weapon")
    @Mapping(target = "toolProficiency", source = "request.proficiency.tool")
    @Mapping(target = "skillProficiency", source = "request.proficiency.skill")
    @Mapping(target = "equipment", source = "request.equipment")
    @Mapping(target = "casterType", source = "request.casterType")
    @Mapping(target = "primaryCharacteristics", source = "request.primaryCharacteristics")
    @Mapping(target = "multiclassProficiency", source = "request.multiclassProficiency")
    @interface ToEntityMapping {
    }

    @Named("toPrimaryCharacteristics")
    default String toPrimaryCharacteristics(Set<Ability> abilities) {
        var list = abilities.stream().toList();
        if (list.isEmpty()) {
            return "";
        }
        if (list.size() == 1) {
            return list.getFirst().getName();
        }
        if (abilities.size() == 2) {
            return list.getFirst().getName() + " или " + list.get(1).getName();
        }

        String prefix = abilities.stream()
                .map(Ability::getName)
                .limit(abilities.size() - 1)
                .collect(Collectors.joining(", "));

        return prefix + " или " + list.getLast().getName();
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

    @Named("toFeatureEntities")
    default List<ClassFeature> toFeatureEntities(List<ClassFeatureRequest> features) {
        return features.stream().map(ClassFeature::new).toList();
    }

    default List<ClassFeatureDto> toFeaturesDto(CharacterClass characterClass) {
        boolean isSubclass = !Objects.isNull(characterClass.getParent());
        List<ClassFeatureDto> parentFeaturesDtos = Optional.ofNullable(characterClass.getParent())
                .map(CharacterClass::getFeatures)
                .orElse(List.of()).stream()
                .filter(classFeature -> !classFeature.isHideInSubclasses())
                .map(f -> new ClassFeatureDto(f, false))
                .collect(Collectors.toList());
        List<ClassFeatureDto> classFeatureDtos = characterClass.getFeatures().stream()
                .map(f -> new ClassFeatureDto(f, isSubclass))
                .collect(Collectors.toList());

        return Stream.of(parentFeaturesDtos, classFeatureDtos)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(ClassFeatureDto::getLevel))
                .collect(Collectors.toList());
    }

    @Named("hasSubclasses")
    default boolean hasSubclasses(Collection<CharacterClass> subclasses) {
        return !CollectionUtils.isEmpty(subclasses);
    }

    @Named("toImageUrl")
    default String toImageUrl(CharacterClass characterClass) {
        if (characterClass.getParent() == null || StringUtils.hasText(characterClass.getImageUrl())) {
            return characterClass.getImageUrl();
        }
        return characterClass.getParent().getImageUrl();
    }

    @Named("getLevels")
    default List<Integer> getLevels(List<ClassFeature> features) {
        for (ClassFeature classFeature : features) {
            if (classFeature.isAbilityImprovement()) {
                List<Integer> levels = new ArrayList<>(classFeature.getScaling().size() + 1);
                levels.add(classFeature.getLevel());
                for (var sub : classFeature.getScaling()) {
                    levels.add(sub.getLevel());
                }
                return levels;
            }
        }
        return Collections.emptyList();
    }

    @Named("getAbilityBonus")
    default List<AbilityBonusResponse> getAbilityBonus(List<ClassFeature> features) {
        return features.stream()
                .filter(f -> f.getAbilityBonus() != null && !f.getAbilityBonus().getAbilities().isEmpty())
                .map(f -> AbilityBonusResponse.builder()
                                .level(f.getLevel())
                                .bonus(f.getAbilityBonus().getBonus())
                                .abilities(f.getAbilityBonus().getAbilities())
                                .upto(f.getAbilityBonus().getUpto())
                        .build())
                .toList();
    }
}
