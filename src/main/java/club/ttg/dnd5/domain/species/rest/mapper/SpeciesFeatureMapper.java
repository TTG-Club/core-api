package club.ttg.dnd5.domain.species.rest.mapper;

import club.ttg.dnd5.domain.species.model.SpeciesFeature;
import club.ttg.dnd5.domain.species.rest.dto.FeatureRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesFeatureResponse;
import club.ttg.dnd5.util.SlugifyUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SpeciesFeatureMapper {
    @Mapping(target = "name.name", source = "name")
    SpeciesFeatureResponse toDto(SpeciesFeature speciesFeature);

    @Mapping(target = "url", source = "name", qualifiedByName = "generateUrl")
    @Mapping(target = "english", source = "name", qualifiedByName = "generateUrl")
    //@Mapping(source = "source.name.name", target = "source.name")
    SpeciesFeature toEntity(FeatureRequest request);

    @Named("generateUrl")
    default String generateUrl(String name) {
        return SlugifyUtil.getSlug(name);
    }
}
