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
    @Mapping(source = "size.text", target = "properties.size")
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
    @Mapping(source = "name.alternative", target = "alternative", qualifiedByName = "collectToString")
    Species toEntity(SpeciesRequest request);

    @Named("collectToString")
    default String collectToString(Collection<String> names) {
        return String.join(" ", names);
    }
}
