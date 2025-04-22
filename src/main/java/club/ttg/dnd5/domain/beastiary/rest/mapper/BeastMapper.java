package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.Beast;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastShortResponse;
import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Mapper(componentModel = "spring")
public interface BeastMapper {
    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    BeastShortResponse toShort(Beast beast);

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    BeastDetailResponse toDetail(Beast beast);

    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    BeastRequest toRequest(Beast byUrl);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    Beast toEntity(BeastRequest request, Book source);

    @Named("collectToString")
    default String collectToString(Collection<String> names) {
        return String.join(" ", names);
    }

    @Named("altToCollection")
    default Collection<String> altToCollection(String string) {
        if(StringUtils.isEmpty(string)) {
            return Collections.emptyList();
        }
        return Arrays.asList(string.split(";"));
    }
}
