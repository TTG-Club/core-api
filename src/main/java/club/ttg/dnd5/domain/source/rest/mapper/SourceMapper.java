package club.ttg.dnd5.domain.source.rest.mapper;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.rest.dto.SourceDetailResponse;
import club.ttg.dnd5.domain.source.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", uses = {
        BaseMapping.class
})
public interface SourceMapper {
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = "acronym", target = "name.label")
    ShortResponse toShort(Source source);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "source.name", ignore = true)
    @Mapping(source = "type.group", target = "source.group.name")
    @Mapping(source = "type.label", target = "source.group.label")
    @Mapping(source = "name", target = "source.name.name")
    @Mapping(source = "english", target = "source.name.english")
    @Mapping(source = "acronym", target = "source.name.label")
    @Mapping(source = "type.name", target = "type")
    @Mapping(source = "published", target = "published", qualifiedByName = "toPublishedString")
    SourceDetailResponse toDetail(Source source);

    @BaseMapping.BaseEntityNameMapping
    Source toEntity(SourceRequest request);

    @BaseMapping.BaseEntityNameMapping
    void toEntity(SourceRequest request, @MappingTarget Source source);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = "acronym", target = "name.label")
    SourceRequest toRequest(Source source);

    @Named("toPublishedString")
    default String toPublishedString(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}
