package club.ttg.dnd5.utills.character;

import club.ttg.dnd5.dictionary.character.FeatCategory;
import club.ttg.dnd5.dto.NameDto;
import club.ttg.dnd5.dto.character.FeatDto;
import club.ttg.dnd5.model.character.Feat;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.BiFunction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeatConverter {
    public static final BiFunction<FeatDto, Feat, Feat> MAP_DTO_TO_ENTITY = (dto, entity) -> {
        entity.setCategory(FeatCategory.valueOf(dto.getCategory().getEng()));
        entity.setPrerequisite(dto.getPrerequisite());
        return entity;
    };

    public static final BiFunction<FeatDto, Feat, FeatDto> MAP_ENTITY_TO_DTO_ = (dto, entity) -> {
        dto.setCategory(NameDto.builder().rus(entity.getCategory().getName()).eng(entity.getCategory().name()).build());

        return dto;
    };
}
