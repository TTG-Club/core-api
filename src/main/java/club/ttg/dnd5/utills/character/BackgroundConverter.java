package club.ttg.dnd5.utills.character;

import club.ttg.dnd5.dictionary.Ability;
import club.ttg.dnd5.dictionary.Skill;
import club.ttg.dnd5.dto.NameDto;
import club.ttg.dnd5.dto.character.BackgroundDto;
import club.ttg.dnd5.model.character.Background;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.BiFunction;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BackgroundConverter {
    public static final BiFunction<BackgroundDto, Background, Background> MAP_DTO_TO_ENTITY = (dto, entity) -> {
        entity.setAbilities(dto.getAbilityScores().stream().map(a -> Ability.valueOf(a.getEng())).collect(Collectors.toSet()));
        entity.setSkillProficiencies(dto.getSkillProficiencies().stream().map(a -> Skill.valueOf(a.getEng())).collect(Collectors.toSet()));
        entity.setToolProficiency(dto.getToolProficiency());
        return entity;
    };

    public static final BiFunction<BackgroundDto, Background, BackgroundDto> MAP_ENTITY_TO_DTO_ = (dto, entity) -> {
        dto.setAbilityScores(entity.getAbilities().stream().map(a -> NameDto.builder().rus(a.getName()).eng(a.name()).build()).toList());
        dto.setSkillProficiencies(entity.getSkillProficiencies().stream().map(a -> NameDto.builder().rus(a.getName()).eng(a.name()).build()).toList());
        dto.setToolProficiency(entity.getToolProficiency());
        return dto;
    };
}
