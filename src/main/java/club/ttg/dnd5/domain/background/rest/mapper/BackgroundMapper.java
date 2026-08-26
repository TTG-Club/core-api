package club.ttg.dnd5.domain.background.rest.mapper;

import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundSelectResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundShortResponse;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.rest.mapper.EquipmentMapping;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {EquipmentMapping.class, BaseMapping.class},
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface BackgroundMapper {
    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = "abilities", target = "abilityScores", qualifiedByName = "abilitiesToString")
    @Mapping(source = "feat.name", target = "featName")
    @Mapping(source = "feat.url", target = "featUrl")
    BackgroundShortResponse toShort(Background background);

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = ".", target = "feat", qualifiedByName = "featToMarkup")
    @Mapping(source = "abilities", target = "abilityScores", qualifiedByName = "abilitiesToString")
    @Mapping(source = "skillProficiencies", target = "skillProficiencies", qualifiedByName = "skillsToString")
    @Mapping(source = "startingEquipment", target = "startingEquipment", qualifiedByName = "toEquipmentOptionDtos")
    BackgroundDetailResponse toDetail(Background background);

    @BaseMapping.BaseRequestNameMapping
    @Mapping(source = "abilities", target = "abilityScores")
    @Mapping(source = "feat.url", target = "featUrl")
    @Mapping(source = "skillProficiencies", target = "skillsProficiencies")
    @Mapping(source = "source.url", target = "source.url")
    @Mapping(source = "sourcePage", target = "source.page")
    @Mapping(source = "startingEquipment", target = "startingEquipment", qualifiedByName = "toEquipmentForm")
    BackgroundRequest toRequest(Background background);

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = "abilities", target = "abilityScores")
    @Mapping(source = "feat.url", target = "featUrl")
    @Mapping(source = "skillProficiencies", target = "skillsProficiencies")
    BackgroundSelectResponse toSelect(Background background);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.original", target = "original")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(source = "request.imageUrl", target = "imageUrl")
    @Mapping(source = "request.abilityScores", target = "abilities")
    @Mapping(source = "request.skillsProficiencies", target = "skillProficiencies")
    @Mapping(source = "request.srdVersion", target = "srdVersion")
    @Mapping(source = "feat", target = "feat")
    @Mapping(source = "source", target = "source")
    @Mapping(source = "request.mechanics", target = "mechanics")
    @Mapping(source = "request.activeEffects", target = "activeEffects")
    @Mapping(source = "request.startingEquipment", target = "startingEquipment", qualifiedByName = "toEquipmentEntities")
    Background toEntity(BackgroundRequest request, Feat feat, Source source);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(target = "url", ignore = true)
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.original", target = "original")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(source = "request.imageUrl", target = "imageUrl")
    @Mapping(source = "request.abilityScores", target = "abilities")
    @Mapping(source = "request.skillsProficiencies", target = "skillProficiencies")
    @Mapping(source = "request.srdVersion", target = "srdVersion")
    @Mapping(source = "feat", target = "feat")
    @Mapping(source = "source", target = "source")
    @Mapping(source = "request.mechanics", target = "mechanics")
    @Mapping(source = "request.activeEffects", target = "activeEffects")
    @Mapping(source = "request.startingEquipment", target = "startingEquipment", qualifiedByName = "toEquipmentEntities")
    void updateEntity(BackgroundRequest request, Feat feat, Source source, @MappingTarget Background background);

    @Named("featToMarkup")
    default String featToMarkup(Background background) {
        Feat feat = background.getFeat();
        String suffix = background.getFeatSuffix();
        if (feat == null) {
            return suffix != null ? "\"%s\"".formatted(suffix) : null;
        }
        return "\"{@feat %s [%s]|url:%s}%s\"".formatted(feat.getName(), feat.getEnglish(), feat.getUrl(),
                suffix != null ? " " + suffix : "");
    }

    @Named("abilitiesToString")
    default String getAbilitiesToString(Set<Ability> skillProficiencies) {
        return skillProficiencies.stream()
                .sorted(Comparator.comparing(Enum::ordinal))
                .map(Ability::getName)
                .collect(Collectors.joining(", "));
    }

    @Named("skillsToString")
    default String getSkillToString(Set<Skill> skillProficiencies) {
        return skillProficiencies.stream()
                .sorted(Comparator.comparing(Skill::ordinal))
                .map(Skill::getName)
                .collect(Collectors.joining(", "));
    }
}
