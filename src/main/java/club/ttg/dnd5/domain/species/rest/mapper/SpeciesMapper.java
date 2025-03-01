package club.ttg.dnd5.domain.species.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.BeastSize;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.model.SpeciesSize;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesDetailResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesShortResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = SpeciesFeatureMapper.class)
public interface SpeciesMapper {
    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "darkVision", target = "properties.darkVision")
    @Mapping(source = "sizes", target = "properties.size", qualifiedByName = "sizesToString")
    SpeciesDetailResponse toDetailDto(Species species);

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @Mapping(source = "source.bookInfo.type.name", target = "source.group.name")
    @Mapping(source = "imageUrl", target = "image")
    @Mapping(source = "source.bookInfo.sourceAcronym", target = "source.name.name")
    @Mapping(source = "updatedAt", target = "updatedAt")
    SpeciesShortResponse toShortDto(Species species);

    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    Species toEntity(SpeciesRequest request);

    @Named("sizesToString")
    default String sizesToString(Collection<SpeciesSize> sizes) {
        return sizes.stream().map(SpeciesSize::getSizeString).collect(Collectors.joining(" и "));
    }
}
