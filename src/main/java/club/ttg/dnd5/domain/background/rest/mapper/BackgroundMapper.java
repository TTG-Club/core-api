package club.ttg.dnd5.domain.background.rest.mapper;

import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dto.ShortResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BackgroundMapper {
    @Mapping(target = "name.name", source = "name")
    @Mapping(target = "name.english", source = "english")
    ShortResponse toShortDto(Background background);
    @Mapping(target = "name.name", source = "name")
    @Mapping(target = "feat", source = "feat.name")
    @Mapping(target = "skillProficiencies", source = "skillProficiencies", qualifiedByName = "setToString")
    BackgroundDetailResponse toDetailDto(Background background);

    @Mapping(target = "name", source = "name.name")
    Background toEntity(BackgroundRequest request);

    @Named("setToString")
    default String collectionToString(Set<Skill> skillProficiencies) {
        return skillProficiencies.stream().map(Skill::getName).collect(Collectors.joining(", "));
    }
}
