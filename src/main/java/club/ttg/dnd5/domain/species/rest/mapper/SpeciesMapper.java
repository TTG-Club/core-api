package club.ttg.dnd5.domain.species.rest.mapper;

import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesDetailResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = SpeciesFeatureMapper.class)
public interface SpeciesMapper {
    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "type.name", target = "properties.type")
    @Mapping(source = ".", target = "properties.speed",  qualifiedByName = "toSpeed")
    @Mapping(source = "size.size", target = "properties.size", qualifiedByName = "sizeToString")

    @BaseMapping.BaseSourceMapping
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
    @Mapping(source = "parent", target = "parent.url")
    @Mapping(source = "properties.size", target = "size.size")
    @Mapping(source = "properties.type", target = "type")
    @Mapping(source = "properties.movementAttributes.base", target = "speed")
    @Mapping(source = "properties.movementAttributes.fly", target = "fly")
    @Mapping(source = "properties.movementAttributes.climb", target = "climb")
    @Mapping(source = "properties.movementAttributes.swim", target = "swim")
    @Mapping(source = "features", target = "features")
    @Mapping(target = "parent", ignore = true)
    @Mapping(source = "name.alternative", target = "alternative", qualifiedByName = "collectToString")
    Species toEntity(SpeciesRequest request);

    @Named("toSpeed")
    default String toSpeed(Species species) {
        return species.getSpeed() + " футов";
    }

    @Named("sizeToString")
    default String sizesToString(List<Size> sizes) {
        return sizes.stream()
                .map(Size::getName)
                .collect(Collectors.joining(" или ") );
    }

    @Named("collectToString")
    default String collectToString(Collection<String> names) {
        return String.join(" ", names);
    }
}
