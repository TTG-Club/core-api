package club.ttg.dnd5.domain.clazz.rest.mapper;

import club.ttg.dnd5.domain.clazz.model.ClassFeature;
import club.ttg.dnd5.domain.clazz.rest.dto.ClassFeatureResponse;
import club.ttg.dnd5.domain.clazz.rest.dto.ClassFeatureRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClassFeatureMapper {

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @Mapping(source = "source.name", target = "source.name.name")
    ClassFeatureResponse toShortDto(ClassFeature classFeature);


    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    ClassFeature toEntity(ClassFeatureRequest classFeatureDto);

}
