package club.ttg.dnd5.domain.feat.rest.mapper;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeatMapper {
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    ShortResponse toShortDto(Feat feat);
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    FeatDetailResponse toDetailDto(Feat feat);

    @Mapping(source = "name.name", target = "name")
    Feat toEntity(FeatRequest dto);
}
