package club.ttg.dnd5.utills.converters.spell;

import club.ttg.dnd5.dto.NameValueDto;
import club.ttg.dnd5.dto.spell.SpellDto;
import club.ttg.dnd5.model.spell.Spell;

import java.util.function.BiFunction;

public class SpellConverter {
    public static final BiFunction<SpellDto, Spell, Spell> MAP_DTO_TO_ENTITY = (dto, entity) -> {
        entity.setLevel((Short) dto.getLevel().getValue());
        entity.setSchool(new MagicSchoolConverter().convertToEntity(dto.getSchool()));
        return entity;
    };

    public static final BiFunction<SpellDto, Spell, SpellDto> MAP_ENTITY_TO_DTO_ = (dto, entity) -> {
        dto.setLevel(NameValueDto.builder()
                        .name(entity.getLevel() == 0 ? "заговор" : String.valueOf(entity.getLevel()))
                        .value(entity.getLevel())
                .build());
        dto.setSchool(new MagicSchoolConverter().convertToDto(entity.getSchool()));

        return dto;
    };
}
