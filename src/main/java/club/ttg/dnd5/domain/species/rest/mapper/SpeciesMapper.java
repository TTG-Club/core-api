package club.ttg.dnd5.domain.species.rest.mapper;

import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.model.SpeciesSize;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesDetailResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesShortResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesSizeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.CollectionUtils;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = SpeciesFeatureMapper.class)
public interface SpeciesMapper {
    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "type.name", target = "properties.type")
    @Mapping(source = ".", target = "properties.speed",  qualifiedByName = "toSpeed")
    @Mapping(source = "size.text", target = "properties.size")

    @Mapping(source = "source.type.group", target = "source.group.name")
    @Mapping(source = "source.type.label", target = "source.group.label")
    @Mapping(source = "source.name", target = "source.name.name")
    @Mapping(source = "source.englishName", target = "source.name.english")
    @Mapping(source = "source.sourceAcronym", target = "source.name.label")
    @Mapping(source = "galleryUrl", target = "gallery")
    @Mapping(source = "lineages", target = "hasLineages", qualifiedByName = "hasLineages")
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
    @Mapping(source = "lineages", target = "hasLineages", qualifiedByName = "hasLineages")
    SpeciesShortResponse toShortDto(Species species);

    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    @Mapping(target = "parent", ignore = true)
    @Mapping(source = "properties.sizes", target = "size", qualifiedByName = "collectSizes")
    @Mapping(source = "properties.type", target = "type")
    @Mapping(source = "properties.movementAttributes.base", target = "speed")
    @Mapping(source = "properties.movementAttributes.fly", target = "fly")
    @Mapping(source = "properties.movementAttributes.climb", target = "climb")
    @Mapping(source = "properties.movementAttributes.swim", target = "swim")
    @Mapping(source = "features", target = "features")
    @Mapping(source = "name.alternative", target = "alternative", qualifiedByName = "collectToString")
    @Mapping(source = "galleryUrl", target = "galleryUrl")
    Species toEntity(SpeciesRequest request);

    @Named("hasLineages")
    default boolean hasLineages(Collection<Species> lineages) {
        return !CollectionUtils.isEmpty(lineages);
    }

    @Named("collectSizes")
    default SpeciesSize toSizeString(Collection<SpeciesSizeDto> sizes) {
        var size = new SpeciesSize();
        size.setSize(size.getSize());
        var sizeString = sizes.stream()
                .map(s -> String.format("%s (около %d-%d футов в высоту)", s.getType().getName(), s.getFrom(), s.getTo()))
                .collect(Collectors.joining(" или "));
        if (sizes.size() > 1) {
            sizeString += ", выбирается при выборе этого вида";
        }
        size.setText(sizeString);
        return size;
    }

    @Named("toSpeed")
    default String toSpeed(Species species) {
        return species.getSpeed() + " футов";
    }

    @Named("collectToString")
    default String collectToString(Collection<String> names) {
        return String.join(" ", names);
    }
}
