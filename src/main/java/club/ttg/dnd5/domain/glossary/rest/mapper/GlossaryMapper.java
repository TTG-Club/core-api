package club.ttg.dnd5.domain.glossary.rest.mapper;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryDetailedResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryShortResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.create.GlossaryRequest;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


@Mapper(componentModel = "spring")
public interface GlossaryMapper {

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(target = "tagCategory", source = "tagCategory", qualifiedByName = "capitalize")
    GlossaryDetailedResponse toDetail(Glossary glossary);

    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    GlossaryRequest toRequest(Glossary glossary);

    @ToEntityMapping
    Glossary toEntity(GlossaryRequest request, Book source);

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(target = "tagCategory", source = "tagCategory", qualifiedByName = "capitalize")
    GlossaryShortResponse toShort(Glossary glossary);

    @Retention(RetentionPolicy.SOURCE)
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    @BaseMapping.BaseEntityNameMapping
    @interface ToEntityMapping {
    }

    @Named("collectToString")
    default String collectToString(Collection<String> names) {
        return String.join(" ", names);
    }

    @Named("capitalize")
    default String capitalize(String string) {
        return StringUtils.capitalize(string);
    }

    @Named("altToCollection")
    default Collection<String> altToCollection(String string) {
        if(org.apache.commons.lang3.StringUtils.isEmpty(string)) {
            return Collections.emptyList();
        }
        return Arrays.asList(string.split(";"));
    }
}
