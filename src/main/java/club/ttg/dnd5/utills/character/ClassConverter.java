package club.ttg.dnd5.utills.character;

import club.ttg.dnd5.dictionary.Ability;
import club.ttg.dnd5.dictionary.Dice;
import club.ttg.dnd5.dictionary.Skill;
import club.ttg.dnd5.dto.NameDto;
import club.ttg.dnd5.dto.character.ClassDto;
import club.ttg.dnd5.dto.character.ClassMasteryDto;
import club.ttg.dnd5.model.character.ClassCharacter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassConverter {
    public static final BiFunction<ClassDto, ClassCharacter, ClassCharacter> MAP_DTO_TO_ENTITY = (dto, entity) -> {
        entity.setMainAbility(Ability.valueOf(dto.getMainAbility().getEng()));
        if (Objects.nonNull(dto.getMastery())) {
            entity.setArmorMastery(dto.getMastery().getArmor());
            entity.setWeaponMastery(dto.getMastery().getWeapon());
            entity.setToolMastery(dto.getMastery().getTool());
            entity.setSavingThrowMastery(dto.getMastery().getSavingThrow()
                    .stream()
                    .map(NameDto::getEng)
                    .map(Ability::valueOf)
                    .collect(Collectors.toSet()));
            entity.setAvailableSkills(dto.getMastery().getAvailableSkills()
                    .stream()
                    .map(NameDto::getEng)
                    .map(Skill::valueOf)
                    .collect(Collectors.toSet()));
            entity.setCountSkillAvailable(dto.getMastery().getCountAvailableSkills());
        }
        entity.setEquipment(dto.getStartEquipment());
        entity.setHitDice(Dice.parse(dto.getHitDice()));
        return entity;
    };

    public static final BiFunction<ClassDto, ClassCharacter, ClassDto> MAP_ENTITY_TO_DTO_ = (dto, entity) -> {
        dto.setMainAbility(NameDto.builder().rus(entity.getMainAbility().getName()).eng(entity.getMainAbility().name()).build());
        dto.setHitDice(entity.getHitDice().getName());
        var mastery = new ClassMasteryDto();
        mastery.setArmor(entity.getArmorMastery());
        mastery.setWeapon(entity.getWeaponMastery());
        mastery.setTool(entity.getToolMastery());
        mastery.setSavingThrow(entity.getSavingThrowMastery()
                .stream()
                .map(t -> NameDto.builder().rus(t.getName()).eng(t.name()).build())
                .collect(Collectors.toSet()));
        mastery.setAvailableSkills(entity.getAvailableSkills()
                .stream()
                .map(t -> NameDto.builder().rus(t.getCyrillicName()).eng(t.name()).build())
                .collect(Collectors.toSet()));
        dto.setMastery(mastery);
        dto.setStartEquipment(entity.getEquipment());
        return dto;
    };
}
