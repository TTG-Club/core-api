package club.ttg.dnd5.domain.glossary.rest.mapper;

import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryDetailedResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryShortResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.create.GlossaryRequest;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;


@Mapper(componentModel = "spring")
public interface GlossaryMapper {

    @BaseMapping.BaseShortResponseNameMapping
    GlossaryDetailedResponse toDetail(Glossary glossary);

    @Mapping(source = "name", target = "name.name")
    GlossaryRequest toRequest(Glossary glossary);

    @ToEntityMapping
    Glossary toEntity(GlossaryRequest request);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "tags", source = "tags")
    GlossaryShortResponse toShort(Glossary glossary);

    @ToEntityMapping
    void updateEntity(@MappingTarget Glossary glossary, GlossaryRequest request);

    @Retention(RetentionPolicy.SOURCE)
    @BaseMapping.BaseEntityNameMapping
    @interface ToEntityMapping {
    }

    @Named("collectToString")
    default String collectToString(Collection<String> names) {
        return String.join(" ", names);
    }
}
