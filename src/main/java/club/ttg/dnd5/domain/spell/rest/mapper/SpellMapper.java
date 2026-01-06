package club.ttg.dnd5.domain.spell.rest.mapper;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.spell.model.MaterialComponent;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.model.SpellCastingTime;
import club.ttg.dnd5.domain.spell.model.SpellComponents;
import club.ttg.dnd5.domain.spell.model.SpellDistance;
import club.ttg.dnd5.domain.spell.model.SpellDuration;
import club.ttg.dnd5.domain.spell.model.enums.CastingUnit;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.dto.create.SpellRequest;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.util.StringUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mapper(uses = {SpellComponentsMapper.class, SpellAffiliationMapper.class, BaseMapping.class}, componentModel = "spring")
public interface SpellMapper
{
    @ToEntityMapping
    Spell toEntity(SpellRequest request,
                   Source source,
                   java.util.List<CharacterClass> classes,
                   java.util.List<CharacterClass> subclasses,
                   java.util.List<Species> species,
                   java.util.List<Species> lineages);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "name", source = "request.name.name")
    @Mapping(target = "english", source = "request.name.english")
    @Mapping(target = "alternative", source = "request.name.alternative", qualifiedByName = "joinAlternative")
    void updateEntity(@MappingTarget Spell target, SpellRequest request);

    @Mapping(target = "school", source = "school.school.name", qualifiedByName = "capitalize")
    @Mapping(target = "additionalType", source = "school.additionalType")
    @Mapping(target = "concentration", source = "duration", qualifiedByName = "isConcentration")
    @Mapping(target = "ritual", source = "castingTime", qualifiedByName = "isRitual")
    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    SpellShortResponse toShort(Spell spell);

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "school", source = "school.school.name", qualifiedByName = "capitalize")
    @Mapping(target = "additionalType", source = "school.additionalType")
    @Mapping(target = "castingTime", source = ".", qualifiedByName = "castingTimeToString")
    @Mapping(target = "duration", source = ".", qualifiedByName = "durationToString")
    @Mapping(target = "range", source = ".", qualifiedByName = "distanceToString")
    @Mapping(target = "affiliation", source = ".")
    SpellDetailedResponse toDetail(Spell spell);

    @Named("isConcentration")
    default Boolean isConcentration(Collection<SpellDuration> durations)
    {
        return durations.stream()
                .map(SpellDuration::getConcentration)
                .anyMatch(Predicate.isEqual(true));
    }

    @Named("isRitual")
    default Boolean isRitual(Collection<SpellCastingTime> castingTimes)
    {
        return castingTimes.stream()
                .map(SpellCastingTime::getUnit)
                .anyMatch(u -> CastingUnit.RITUAL == u);
    }

    @Named("castingTimeToString")
    default String castingTimeToString(Spell spell)
    {
        return spell.getCastingTime().stream()
                .map(SpellCastingTime::toString)
                .collect(Collectors.joining(" или "));
    }

    @Named("durationToString")
    default String durationToString(Spell spell)
    {
        return spell.getDuration().stream()
                .map(SpellDuration::toString)
                .collect(Collectors.joining(" или "));
    }

    @Named("distanceToString")
    default String distanceToString(Spell spell)
    {
        return spell.getRange().stream()
                .map(SpellDistance::toString)
                .collect(Collectors.joining(" или "));
    }

    @BaseMapping.BaseRequestNameMapping
    @Mapping(source = "source.url", target = "source.url")
    @Mapping(source = "sourcePage", target = "source.page")
    @Mapping(source = "school.school", target = "school")
    @Mapping(source = ".", target = "affiliations")
    SpellRequest toRequest(Spell spell);

    @Retention(RetentionPolicy.SOURCE)
    @BaseMapping.BaseEntityNameMapping
    @Mapping(target = "url", source = "request.url")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "sourcePage", source = "request.source.page")
    @Mapping(target = "school.school", source = "request.school")
    @Mapping(target = "components", source = "request.components", qualifiedByName = "setNull")
    @Mapping(target = "range", source = "request.range")
    @Mapping(target = "castingTime", source = "request.castingTime")
    @Mapping(target = "duration", source = "request.duration")
    @Mapping(target = "upper", source = "request.upper")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "speciesAffiliation", source = "species")
    @Mapping(target = "lineagesAffiliation", source = "lineages")
    @Mapping(target = "classAffiliation", source = "classes")
    @Mapping(target = "subclassAffiliation", source = "subclasses")
    @interface ToEntityMapping
    {
    }

    @Named("setNull")
    default SpellComponents setNull(SpellComponents components)
    {
        if (components == null)
        {
            return null;
        }

        if (Optional.ofNullable(components.getM())
                .map(MaterialComponent::getText)
                .orElse("")
                .isEmpty())
        {
            components.setM(null);
        }

        return components;
    }

    @Named("capitalize")
    default String capitalize(String string)
    {
        return StringUtils.capitalize(string);
    }

    @Named("joinAlternative")
    default String joinAlternative(Collection<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return null;
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("; "));
    }

}
