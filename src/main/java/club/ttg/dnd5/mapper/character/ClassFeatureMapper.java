package club.ttg.dnd5.mapper.character;

import club.ttg.dnd5.dto.character.ClassFeatureDto;
import club.ttg.dnd5.model.character.ClassFeature;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ClassFeatureMapper {
    @Mapping(source = "name", target = "name.rus")
    @Mapping(source = "english", target = "name.eng")
    @Mapping(source = "alternative", target = "name.alt")
    ClassFeatureDto toDto(ClassFeature classFeature);

    @Mapping(source = "name.rus", target = "name")
    @Mapping(source = "name.eng", target = "english")
    @Mapping(source = "name.alt", target = "alternative")
    ClassFeature toEntityDto(ClassFeatureDto classFeatureDto);
}
