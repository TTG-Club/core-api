package club.ttg.dnd5.domain.character_class.rest.mapper;

import club.ttg.dnd5.domain.character_class.model.ClassFeature;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassFeatureDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = "spring")
public interface ClassFeatureMapper {
    ClassFeatureDto toDto(ClassFeature classFeature, boolean isSubclass);
}
