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

import java.util.Arrays;
import java.util.Collection;

@Mapper(componentModel = "spring")
public interface FeatMapper {

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = "category.name", target = "category")
    FeatShortResponse toShortDto(Feat feat);

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = "category.name", target = "category")
    FeatDetailResponse toDetailDto(Feat feat);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.updatedAt", target = "updatedAt")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    Feat toEntity(FeatRequest request, Book source);

    @Named("collectToString")
    default String collectToString(Collection<String> names) {
        return String.join(";", names);
    }

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @Mapping(source = "alternative", target = "name.alternative", qualifiedByName = "altToCollection")
    FeatRequest toRequest(Feat feat);

    @Named("altToCollection")
    default Collection<String> altToCollection(String string) {
        return Arrays.asList(string.split(";"));
    }

}
