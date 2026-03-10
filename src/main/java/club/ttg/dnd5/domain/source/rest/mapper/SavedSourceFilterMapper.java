package club.ttg.dnd5.domain.source.rest.mapper;


import club.ttg.dnd5.domain.filter.model.SourceFilterInfo;
import club.ttg.dnd5.domain.source.model.filter.SourceSavedFilter;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceSavedFilterResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = {
        BaseMapping.class
})
public interface SavedSourceFilterMapper {

    SourceSavedFilterResponse toResponse(SourceSavedFilter filter);

    @Mapping(target = "defaultFilter", constant = "true")
    @Mapping(target = "type", constant = "SOURCE_FILTER")
    @Mapping(target = "filter", source = "filterInfo")
    @Mapping(target = "userId", source = "userId")
    SourceSavedFilter toEntity(SourceFilterInfo filterInfo, UUID userId);

    @Mapping(target = "defaultFilter", constant = "true")
    @Mapping(target = "type", constant = "SOURCE_FILTER")
    @Mapping(target = "filter", source="filterInfo")
    SourceSavedFilter update(@MappingTarget SourceSavedFilter filter, SourceFilterInfo filterInfo);
}
