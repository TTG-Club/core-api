package club.ttg.dnd5.domain.background.rest.mapper;

import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BackgroundMapper {
    @BaseMapping.BaseShortResponseNameMapping
    ShortResponse toShortDto(Background background);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = "feat.name", target = "feat")
    @Mapping(source = "abilities", target = "abilityScores", qualifiedByName = "abilitiesToString")
    @Mapping(source = "skillProficiencies", target = "skillProficiencies", qualifiedByName = "skillsToString")
    BackgroundDetailResponse toDetailDto(Background background);

    @Mapping(target = "name", source = "name.name")
    @Mapping(target = "feat", ignore = true)
    Background toEntity(BackgroundRequest request);

    @Named("abilitiesToString")
    default String getAbilitiesToString(Set<Ability> skillProficiencies) {
        return skillProficiencies.stream().map(Ability::getName).collect(Collectors.joining(", "));
    }

    @Named("skillsToString")
    default String getSkillToString(Set<Skill> skillProficiencies) {
        return skillProficiencies.stream().map(Skill::getName).collect(Collectors.joining(", "));
    }

    default Background toEntity(BackgroundRequest request, Feat feat) {
        Background background = toEntity(request);
        background.setFeat(feat);
        return background;
    }
}
