package club.ttg.dnd5.utills.character;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.clazz.rest.dto.ClassDetailResponse;
import club.ttg.dnd5.domain.clazz.rest.dto.ClassMasteryDto;
import club.ttg.dnd5.domain.clazz.model.ClassCharacter;
import club.ttg.dnd5.domain.common.dto.NameDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassConverter {
    public static final BiFunction<ClassDetailResponse, ClassCharacter, ClassCharacter> MAP_DTO_TO_ENTITY = (dto, entity) -> {
        entity.setMainAbility(dto.getMainAbility());
        if (Objects.nonNull(dto.getMastery())) {
            entity.setArmorMastery(dto.getMastery().getArmor());
            entity.setWeaponMastery(dto.getMastery().getWeapon());
            entity.setToolMastery(dto.getMastery().getTool());
            entity.setSavingThrowMastery(dto.getMastery().getSavingThrow()
                    .stream()
                    .map(NameDto::getEnglish)
                    .map(Ability::valueOf)
                    .collect(Collectors.toSet()));
            entity.setAvailableSkills(dto.getMastery().getAvailableSkills()
                    .stream()
                    .map(NameDto::getEnglish)
                    .map(Skill::valueOf)
                    .collect(Collectors.toSet()));
            entity.setCountSkillAvailable(dto.getMastery().getCountAvailableSkills());
        }
        entity.setEquipment(dto.getStartEquipment());
        entity.setHitDice(Dice.parse(dto.getHitDice()));
        return entity;
    };

    public static final BiFunction<ClassDetailResponse, ClassCharacter, ClassDetailResponse> MAP_ENTITY_TO_DTO_ = (dto, entity) -> {
        dto.setMainAbility(entity.getMainAbility());
        dto.setHitDice(entity.getHitDice().getName());
        var mastery = new ClassMasteryDto();
        mastery.setArmor(entity.getArmorMastery());
        mastery.setWeapon(entity.getWeaponMastery());
        mastery.setTool(entity.getToolMastery());
        mastery.setSavingThrow(entity.getSavingThrowMastery()
                .stream()
                .map(t -> NameDto.builder().name(t.getName()).english(t.name()).build())
                .collect(Collectors.toSet()));
        mastery.setAvailableSkills(entity.getAvailableSkills()
                .stream()
                .map(t -> NameDto.builder().name(t.getName()).english(t.name()).build())
                .collect(Collectors.toSet()));
        dto.setMastery(mastery);
        dto.setStartEquipment(entity.getEquipment());
        return dto;
    };
}
