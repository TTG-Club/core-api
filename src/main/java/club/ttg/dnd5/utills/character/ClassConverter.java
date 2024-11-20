package club.ttg.dnd5.utills.character;

import club.ttg.dnd5.dictionary.Ability;
import club.ttg.dnd5.dictionary.Dice;
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
        if (Objects.nonNull(dto.getMastery())) {
            entity.setArmorMastery(dto.getMastery().getArmor());
            entity.setWeaponMastery(dto.getMastery().getWeapon());
            entity.setToolMastery(dto.getMastery().getTool());
            entity.setSavingThrowMastery(dto.getMastery().getSavingThrow()
                    .stream()
                    .map(Ability::parseShortName)
                    .collect(Collectors.toSet()));
        }
        entity.setEquipment(dto.getEquipment());
        entity.setHitDice(Dice.parse(dto.getHitDice()));
        return entity;
    };

    public static final BiFunction<ClassDto, ClassCharacter, ClassDto> MAP_ENTITY_TO_DTO_ = (dto, entity) -> {
        var mastery = new ClassMasteryDto();
        mastery.setArmor(entity.getArmorMastery());
        mastery.setWeapon(entity.getWeaponMastery());
        mastery.setTool(entity.getToolMastery());
        mastery.setSavingThrow(entity.getSavingThrowMastery()
                .stream()
                .map(Ability::getShortName)
                .toList());
        dto.setMastery(mastery);
        dto.setEquipment(entity.getEquipment());
        dto.setHitDice(entity.getHitDice().getName());
        return dto;
    };
}
