package club.ttg.dnd5.utills.character;

import club.ttg.dnd5.domain.common.dto.NameDto;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.model.Feat;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.BiFunction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeatConverter {
    public static final BiFunction<FeatDetailResponse, Feat, Feat> MAP_DTO_TO_ENTITY = (dto, entity) -> {
        entity.setCategory(FeatCategory.valueOf(dto.getCategory().getEnglish()));
        entity.setPrerequisite(dto.getPrerequisite());
        return entity;
    };

    public static final BiFunction<FeatDetailResponse, Feat, FeatDetailResponse> MAP_ENTITY_TO_DTO_ = (dto, entity) -> {
        dto.setCategory(NameDto.builder()
                .name(entity.getCategory().getName())
                .english(entity.getCategory().name())
                .build());
        return dto;
    };
}
