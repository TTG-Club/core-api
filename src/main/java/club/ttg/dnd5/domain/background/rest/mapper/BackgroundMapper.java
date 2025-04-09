package club.ttg.dnd5.domain.background.rest.mapper;

import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundShortResponse;
import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BackgroundMapper {
    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    BackgroundShortResponse toShort(Background background);

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = "feat.name", target = "feat")
    @Mapping(source = "abilities", target = "abilityScores", qualifiedByName = "abilitiesToString")
    @Mapping(source = "skillProficiencies", target = "skillProficiencies", qualifiedByName = "skillsToString")
    BackgroundDetailResponse toDetail(Background background);

    @BaseMapping.BaseShortResponseNameMapping
    BackgroundRequest toRequest(Background background);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(source = "request.imageUrl", target = "imageUrl")
    @Mapping(source = "source", target = "source")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "source.createdAt", ignore = true)
    @Mapping(target = "source.updatedAt", ignore = true)
    Background toEntity(BackgroundRequest request, Feat feat, Book source);

    @Named("abilitiesToString")
    default String getAbilitiesToString(Set<Ability> skillProficiencies) {
        return skillProficiencies.stream()
                .map(Ability::getName)
                .collect(Collectors.joining(", "));
    }

    @Named("skillsToString")
    default String getSkillToString(Set<Skill> skillProficiencies) {
        return skillProficiencies.stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));
    }

    @Named("collectToString")
    default String collectToString(Collection<String> names) {
        return String.join(";", names);
    }
}
