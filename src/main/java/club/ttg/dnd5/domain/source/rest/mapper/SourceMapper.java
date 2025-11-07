package club.ttg.dnd5.domain.source.rest.mapper;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.rest.dto.SourceDetailResponse;
import club.ttg.dnd5.domain.source.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {
        BaseMapping.class
})
public interface SourceMapper {
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = "acronym", target = "name.label")
    ShortResponse toShort(Source source);

    @BaseMapping.BaseShortResponseNameMapping
    SourceDetailResponse toDetail(Source source);

    @BaseMapping.BaseEntityNameMapping
    Source toEntity(SourceRequest request);

    @BaseMapping.BaseEntityNameMapping
    void toEntity(SourceRequest request, @MappingTarget Source source);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = "acronym", target = "name.label")
    SourceRequest toRequest(Source source);
}
