package club.ttg.dnd5.domain.species.rest.mapper;

import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesDetailResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesShortResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.format.DateTimeFormatter;
import java.util.Collection;

@Mapper(componentModel = "spring", uses = SpeciesFeatureMapper.class)
public interface SpeciesMapper {
    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "darkVision", target = "properties.darkVision")
    //@Mapping(source = "speed", target = "properties.movementAttributes.base")
    @Mapping(source = "fly", target = "properties.movementAttributes.fly")
    //@Mapping(source = "size.text", target = "sizes.")
    @Mapping(source = "source.name", target = "source.group.name")
    @Mapping(source = "source.sourceAcronym", target = "source.name.name")
    SpeciesDetailResponse toDetailDto(Species species);

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @Mapping(source = "imageUrl", target = "image")
    @Mapping(source = "source.type.group", target = "source.group.name")
    @Mapping(source = "source.type.label", target = "source.group.label")
    @Mapping(source = "source.name", target = "source.name.name")
    @Mapping(source = "source.englishName", target = "source.name.english")
    @Mapping(source = "source.sourceAcronym", target = "source.name.label")
    @Mapping(source = "updatedAt", target = "updatedAt")
    SpeciesShortResponse toShortDto(Species species);

    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    @Mapping(source = "properties.sizes", target = "size.size")
    @Mapping(source = "properties.type", target = "type")
    @Mapping(source = "properties.movementAttributes.base", target = "speed")
    @Mapping(source = "properties.movementAttributes.fly", target = "fly")
    @Mapping(source = "properties.movementAttributes.climb", target = "climb")
    @Mapping(source = "properties.movementAttributes.swim", target = "swim")
    @Mapping(source = "properties.darkVision", target = "darkVision")
    @Mapping(source = "features", target = "features")
    @Mapping(source = "name.alternative", target = "alternative", qualifiedByName = "collectToString")
    Species toEntity(SpeciesRequest request);

    @Named("collectToString")
    default String collectToString(Collection<String> names) {
        return String.join(" ", names);
    }
}
