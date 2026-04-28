package club.ttg.dnd5.domain.character_class.rest.mapper;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.character_class.model.*;
import club.ttg.dnd5.domain.character_class.rest.dto.*;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Delimiter;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.rest.dto.select.DiceOptionDto;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {BaseMapping.class}, componentModel = "spring")
public interface ClassMapper
{
    @Named("toShortResponse")
    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "hasSubclasses", source = "subclasses", qualifiedByName = "hasSubclasses")
    @Mapping(target = "image", source = ".", qualifiedByName = "toImageUrl")
    ClassShortResponse toShort(CharacterClass characterClass);

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
    @Mapping(target = "primaryCharacteristics", source = ".", qualifiedByName = "toPrimaryCharacteristics")
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
    void updateEntity(
            @MappingTarget CharacterClass existingClass,
            ClassRequest request,
            Source source
    );

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
    @Mapping(target = "primaryCharacteristics.values", source = "primaryCharacteristics")
    @Mapping(target = "primaryCharacteristics.delimiter", source = "delimiterPrimary")
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
    @Mapping(target = "primaryCharacteristics", source = "request.primaryCharacteristics.values")
    @Mapping(target = "delimiterPrimary", source = "request.primaryCharacteristics.delimiter")
    @Mapping(target = "multiclassProficiency", source = "request.multiclassProficiency")
    @interface ToEntityMapping
    {
    }

    @Named("toPrimaryCharacteristics")
    default String toPrimaryCharacteristics(CharacterClass characterClass)
    {
        List<Ability> abilities = Optional.ofNullable(characterClass.getPrimaryCharacteristics())
                .orElse(Collections.emptySet())
                .stream()
                .toList();

        if (abilities.isEmpty())
        {
            return "";
        }

        if (abilities.size() == 1)
        {
            return abilities.getFirst().getName();
        }

        Delimiter delimiter = resolvePrimaryDelimiter(characterClass);

        if (abilities.size() == 2)
        {
            return abilities.getFirst().getName()
                    + " "
                    + delimiter.getName()
                    + " "
                    + abilities.get(1).getName();
        }

        String joinDelimiter = delimiter == Delimiter.AND
                ? ", "
                : delimiter.getName() + " ";

        String prefix = abilities.stream()
                .limit(abilities.size() - 1L)
                .map(Ability::getName)
                .collect(Collectors.joining(joinDelimiter));

        return prefix + " " + delimiter.getName() + " " + abilities.getLast().getName();
    }

    default Delimiter resolvePrimaryDelimiter(CharacterClass characterClass)
    {
        if (characterClass.getDelimiterPrimary() != null)
        {
            return characterClass.getDelimiterPrimary();
        }

        CharacterClass parent = characterClass.getParent();
        if (parent != null && parent.getDelimiterPrimary() != null)
        {
            return parent.getDelimiterPrimary();
        }

        return Delimiter.AND;
    }

    @Named("toSavingThrowsString")
    default String toSavingThrowsString(Collection<Ability> savingThrows)
    {
        return savingThrows.stream()
                .map(Ability::getName)
                .collect(Collectors.joining(", "));
    }

    default DiceOptionDto toDiceOptionDto(Dice dice)
    {
        return DiceOptionDto.builder()
                .label(dice.getName())
                .value(dice.name())
                .maxValue(dice.getMaxValue())
                .avg(dice.getAvgValue())
                .build();
    }

    @Named("armorProficiencyToString")
    default String armorProficiencyToString(ArmorProficiency proficiency)
    {
        return proficiency == null ? "" : proficiency.toString();
    }

    @Named("weaponProficiencyToString")
    default String weaponProficiencyToString(WeaponProficiency proficiency)
    {
        if (proficiency == null)
        {
            return "";
        }

        return Stream.of(formatWeaponCategories(proficiency.getCategory()), proficiency.getCustom())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(", "));
    }

    private String formatWeaponCategories(Set<WeaponCategory> categories)
    {
        Set<WeaponCategory> safeCategories = categories == null ? Collections.emptySet() : categories;
        boolean hasSimpleWeapons = safeCategories.contains(WeaponCategory.SIMPLE_MELEE)
                && safeCategories.contains(WeaponCategory.SIMPLE_RANGED);
        boolean hasMaterialWeapons = safeCategories.contains(WeaponCategory.MATERIAL_MELEE)
                && safeCategories.contains(WeaponCategory.MATERIAL_RANGED);

        List<String> names = new ArrayList<>();
        if (hasSimpleWeapons)
        {
            names.add("Простое оружие");
        }
        if (hasMaterialWeapons)
        {
            names.add("Воинское оружие");
        }

        safeCategories.stream()
                .filter(category -> !hasSimpleWeapons
                        || category != WeaponCategory.SIMPLE_MELEE && category != WeaponCategory.SIMPLE_RANGED)
                .filter(category -> !hasMaterialWeapons
                        || category != WeaponCategory.MATERIAL_MELEE && category != WeaponCategory.MATERIAL_RANGED)
                .map(WeaponCategory::getName)
                .forEach(names::add);

        return String.join(", ", names);
    }

    @Named("skillProficiencyToString")
    default String skillProficiencyToString(SkillProficiency proficiency)
    {
        return proficiency == null ? "" : proficiency.toString();
    }

    @Named("toFeatureEntities")
    default List<ClassFeature> toFeatureEntities(List<ClassFeatureRequest> features)
    {
        return features.stream()
                .map(ClassFeature::new)
                .toList();
    }

    default List<ClassFeatureDto> toFeaturesDto(CharacterClass characterClass)
    {
        boolean isSubclass = !Objects.isNull(characterClass.getParent());

        List<ClassFeatureDto> parentFeaturesDtos = Optional.ofNullable(characterClass.getParent())
                .map(CharacterClass::getFeatures)
                .orElse(List.of())
                .stream()
                .filter(classFeature -> !classFeature.isHideInSubclasses())
                .map(feature -> new ClassFeatureDto(feature, false))
                .collect(Collectors.toList());

        List<ClassFeatureDto> classFeatureDtos = characterClass.getFeatures().stream()
                .map(feature -> new ClassFeatureDto(feature, isSubclass))
                .collect(Collectors.toList());

        return Stream.of(parentFeaturesDtos, classFeatureDtos)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(ClassFeatureDto::getLevel))
                .collect(Collectors.toList());
    }

    @Named("hasSubclasses")
    default boolean hasSubclasses(Collection<CharacterClass> subclasses)
    {
        return !CollectionUtils.isEmpty(subclasses);
    }

    @Named("toImageUrl")
    default String toImageUrl(CharacterClass characterClass)
    {
        if (characterClass.getParent() == null || StringUtils.hasText(characterClass.getImageUrl()))
        {
            return characterClass.getImageUrl();
        }

        return characterClass.getParent().getImageUrl();
    }

    @Named("getLevels")
    default List<Integer> getLevels(List<ClassFeature> features)
    {
        for (ClassFeature classFeature : features)
        {
            if (classFeature.isAbilityImprovement())
            {
                List<Integer> levels = new ArrayList<>(classFeature.getScaling().size() + 1);
                levels.add(classFeature.getLevel());

                for (var sub : classFeature.getScaling())
                {
                    levels.add(sub.getLevel());
                }

                return levels;
            }
        }

        return Collections.emptyList();
    }

    @Named("getAbilityBonus")
    default List<AbilityBonusResponse> getAbilityBonus(List<ClassFeature> features)
    {
        return features.stream()
                .filter(feature -> feature.getAbilityBonus() != null && !feature.getAbilityBonus().getAbilities().isEmpty())
                .map(feature -> AbilityBonusResponse.builder()
                        .level(feature.getLevel())
                        .bonus(feature.getAbilityBonus().getBonus())
                        .abilities(feature.getAbilityBonus().getAbilities())
                        .upto(feature.getAbilityBonus().getUpto())
                        .build())
                .toList();
    }
}
