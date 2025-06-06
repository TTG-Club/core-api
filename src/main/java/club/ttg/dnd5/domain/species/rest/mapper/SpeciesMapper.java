package club.ttg.dnd5.domain.species.rest.mapper;

import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesDetailResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesShortResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesSizeDto;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.CollectionUtils;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {SpeciesFeatureMapper.class, CreaturePropertiesMapper.class, BaseMapping.class})
public interface SpeciesMapper {
    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "type.name", target = "properties.type")
    @Mapping(source = ".", target = "properties.speed", qualifiedByName = "toSpeed")
    @Mapping(source = "sizes", target = "properties.size", qualifiedByName = "collectSizes")

    @BaseMapping.BaseSourceMapping
    @Mapping(source = "galleryUrl", target = "gallery")
    @Mapping(source = "lineages", target = "hasLineages", qualifiedByName = "hasLineages")
    SpeciesDetailResponse toDetailDto(Species species);

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @Mapping(source = "imageUrl", target = "image")

    @BaseMapping.BaseSourceMapping
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "lineages", target = "hasLineages", qualifiedByName = "hasLineages")
    SpeciesShortResponse toShortDto(Species species);

    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    @Mapping(target = "parent", ignore = true)
    @Mapping(source = "properties.sizes", target = "sizes")
    @Mapping(source = "properties.type", target = "type")
    @Mapping(source = "properties.movementAttributes.base", target = "speed")
    @Mapping(source = "properties.movementAttributes.fly", target = "fly")
    @Mapping(source = "properties.movementAttributes.climb", target = "climb")
    @Mapping(source = "properties.movementAttributes.swim", target = "swim")
    @Mapping(source = "features", target = "features")
    @Mapping(source = "name.alternative", target = "alternative", qualifiedByName = "collectToString")
    @Mapping(source = "gallery", target = "galleryUrl")
    @Mapping(target = "sourcePage", source = "source.page")
    Species toEntity(SpeciesRequest request);

    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    @Mapping(source = "parent.url", target = "parent")
    @Mapping(source = ".", target = "properties")
    @Mapping(source = "galleryUrl", target = "gallery")
    SpeciesRequest toRequest(Species species);

    @Named("hasLineages")
    default boolean hasLineages(Collection<Species> lineages) {
        return !CollectionUtils.isEmpty(lineages);
    }

    @Named("collectSizes")
    default String toSizeString(Collection<SpeciesSizeDto> sizes) {
        var sizeString = sizes.stream()
                .map(s -> String.format("%s (около %d-%d футов в высоту)", s.getType().getName(), s.getFrom(), s.getTo()))
                .collect(Collectors.joining(" или "));
        if (sizes.size() > 1) {
            sizeString += ", выбирается при выборе этого вида";
        }
        return sizeString;
    }

    @Named("toSpeed")
    default String toSpeed(Species species) {
        return species.getSpeed() + " футов";
    }

}
