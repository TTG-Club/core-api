package club.ttg.dnd5.domain.source.rest.mapper;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.rest.dto.SourceDetailResponse;
import club.ttg.dnd5.domain.source.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.source.rest.dto.SourceShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {
        BaseMapping.class
})
public interface SourceMapper {
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = "acronym", target = "name.label")
    @Mapping(target = "source.name", ignore = true)
    @Mapping(source = "type.group", target = "source.group.name")
    @Mapping(source = "type.label", target = "source.group.label")
    @Mapping(source = "name", target = "source.name.name")
    @Mapping(source = "english", target = "source.name.english")
    @Mapping(source = "acronym", target = "source.name.label")
    SourceShortResponse toShort(Source source);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "source.name", ignore = true)
    @Mapping(source = "type.group", target = "source.group.name")
    @Mapping(source = "type.label", target = "source.group.label")
    @Mapping(source = "name", target = "source.name.name")
    @Mapping(source = "english", target = "source.name.english")
    @Mapping(source = "acronym", target = "source.name.label")
    @Mapping(source = "type.name", target = "type")
    @Mapping(target = "imageUrl", source = "image")
    SourceDetailResponse toDetail(Source source);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "imageUrl", target = "image")
    Source toEntity(SourceRequest request);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "imageUrl", target = "image")
    void toEntity(SourceRequest request, @MappingTarget Source source);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = "acronym", target = "name.label")
    @Mapping(source = "image", target = "imageUrl")
    SourceRequest toRequest(Source source);

}
