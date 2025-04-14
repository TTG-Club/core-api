package club.ttg.dnd5.domain.species.rest.mapper;

import club.ttg.dnd5.domain.species.model.SpeciesFeature;
import club.ttg.dnd5.domain.species.rest.dto.FeatureRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesFeatureResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import club.ttg.dnd5.util.SlugifyUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {BaseMapping.class})
public interface SpeciesFeatureMapper {
    @Mapping(target = "name.name", source = "name")
    @Mapping(target = "name.english", source = "english")
    @Mapping(target = "url", source = "name", defaultValue = "generateUrl")
    SpeciesFeatureResponse toDto(SpeciesFeature speciesFeature);

    @Mapping(target = "name", source = "name.name")
    @Mapping(target = "english", source = "name.english")
    SpeciesFeature toEntity(FeatureRequest request);

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    FeatureRequest toRequest(SpeciesFeature request);

    List<FeatureRequest> toRequests(List<SpeciesFeature> speciesFeatures);

    @Named("generateUrl")
    default String generateUrl(String name) {
        return SlugifyUtil.getSlug(name);
    }
}
