package club.ttg.dnd5.domain.source.rest.mapper;


import club.ttg.dnd5.domain.source.model.filter.SourceSavedFilter;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceSavedFilterRequest;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceSavedFilterResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {
        BaseMapping.class
})
public interface SavedSourceFilterMapper {

    @Mapping(target = "filter", source = "filter")
    SourceSavedFilterResponse toResponse(SourceSavedFilter filter);

    @Mapping(target = "defaultFilter", constant = "true")
    @Mapping(target = "type", constant = "SOURCE_FILTER")
    @Mapping(target = "userId", source = "userId")
    SourceSavedFilter toEntity(SourceSavedFilterRequest request, UUID userId);

    @Mapping(target = "defaultFilter", constant = "true")
    @Mapping(target = "type", constant = "SOURCE_FILTER")
    SourceSavedFilter update(@MappingTarget SourceSavedFilter filter, SourceSavedFilterRequest request);
}
