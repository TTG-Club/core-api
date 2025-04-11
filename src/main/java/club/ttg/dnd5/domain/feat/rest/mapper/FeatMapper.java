package club.ttg.dnd5.domain.feat.rest.mapper;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.domain.feat.rest.dto.FeatShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;

@Mapper(componentModel = "spring", uses = {BaseMapping.class})
public interface FeatMapper {

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = "category.name", target = "category", qualifiedByName = "capitalize")
    FeatShortResponse toShortDto(Feat feat);

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = "category.name", target = "category", qualifiedByName = "capitalize")
    FeatDetailResponse toDetailDto(Feat feat);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.updatedAt", target = "updatedAt")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    Feat toEntity(FeatRequest request, Book source);

    @BaseMapping.BaseRequestNameMapping
    FeatRequest toRequest(Feat feat);

    @Named("capitalize")
    default String capitalize(String string) {
        return StringUtils.capitalize(string);
    }

}
