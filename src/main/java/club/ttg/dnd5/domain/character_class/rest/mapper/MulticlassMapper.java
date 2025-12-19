package club.ttg.dnd5.domain.character_class.rest.mapper;

import club.ttg.dnd5.domain.character_class.model.ArmorProficiency;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.SkillProficiency;
import club.ttg.dnd5.domain.character_class.model.WeaponProficiency;
import club.ttg.dnd5.domain.character_class.rest.dto.MulticlassResponse;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR,  componentModel = "spring")
public interface MulticlassMapper {
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "proficiency.armor", source = "armorProficiency", qualifiedByName = "armorProficiencyToString")
    @Mapping(target = "proficiency.weapon", source = "weaponProficiency", qualifiedByName = "weaponProficiencyToString")
    @Mapping(target = "proficiency.tool", source = "toolProficiency")
    @Mapping(target = "proficiency.skill", source = "skillProficiency", qualifiedByName = "skillProficiencyToString")
    @Mapping(target = "savingThrows", source = "savingThrows", qualifiedByName = "toSavingThrowsString")
    @Mapping(target = "primaryCharacteristics", source = "primaryCharacteristics", qualifiedByName = "toPrimaryCharacteristics")
    @Mapping(target = "characterLevel", ignore = true)
    @Mapping(target = "spellcastingLevel", ignore = true)
    @Mapping(target = "multiclass", ignore = true)
    MulticlassResponse toMulticlassResponse(CharacterClass characterClass);

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
}
