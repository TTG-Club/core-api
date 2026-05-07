package club.ttg.dnd5.domain.spell.rest.mapper;

import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.spell.model.MaterialComponent;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.model.SpellCastingTime;
import club.ttg.dnd5.domain.spell.model.SpellComponents;
import club.ttg.dnd5.domain.spell.model.SpellDuration;
import club.ttg.dnd5.domain.spell.rest.dto.SpellAffiliationDto;
import club.ttg.dnd5.domain.spell.rest.dto.SpellAffiliationResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.dto.create.CreateAffiliationRequest;
import club.ttg.dnd5.domain.spell.rest.dto.create.SpellRequest;
import club.ttg.dnd5.domain.spell.model.enums.CastingUnit;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.util.StringUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {SpellComponentsMapper.class, BaseMapping.class},
        componentModel = "spring"
)
public interface SpellMapper
{
    @ToEntityMapping
    Spell toEntity(
            SpellRequest request,
            Source source,
            Set<CharacterClass> classes,
            Set<CharacterClass> subclasses,
            Set<Species> species,
            Set<Species> lineages,
            Set<Feat> feats
    );

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "url", ignore = true)
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
    @Mapping(target = "castingTime", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "range", ignore = true)
    @Mapping(target = "affiliation", ignore = true)
    SpellDetailedResponse toDetail(Spell spell);

    @AfterMapping
    default void fillDetailFields(Spell spell, @MappingTarget SpellDetailedResponse target)
    {
        target.setCastingTime(joinSpellValues(spell.getCastingTime()));
        target.setDuration(joinSpellValues(spell.getDuration()));
        target.setRange(joinSpellValues(spell.getRange()));
        target.setAffiliation(buildAffiliation(spell));
    }

    default SpellAffiliationResponse buildAffiliation(Spell spell)
    {
        SpellAffiliationResponse response = new SpellAffiliationResponse();
        response.setClasses(mapAffiliations(spell.getClassAffiliation()));
        response.setSubclasses(mapAffiliations(spell.getSubclassAffiliation()));
        response.setSpecies(mapAffiliations(spell.getSpeciesAffiliation()));
        response.setLineages(mapAffiliations(spell.getLineagesAffiliation()));
        response.setFeats(mapAffiliations(spell.getFeatAffiliation()));
        return response;
    }

    default Set<SpellAffiliationDto> mapAffiliations(Set<? extends NamedEntity> entities)
    {
        if (entities == null || entities.isEmpty())
        {
            return Set.of();
        }

        Set<SpellAffiliationDto> result = new TreeSet<>(SpellAffiliationDto.BY_NAME_THEN_SOURCE);

        for (NamedEntity entity : entities)
        {
            SpellAffiliationDto dto = toAffiliationDto(entity);
            if (dto != null)
            {
                result.add(dto);
            }
        }

        return result;
    }

    default SpellAffiliationDto toAffiliationDto(NamedEntity entity)
    {
        if (entity == null)
        {
            return null;
        }

        SpellAffiliationDto dto = new SpellAffiliationDto();
        dto.setUrl(entity.getUrl());

        String name = entity.getName();
        String sourceAcronym = null;

        switch (entity)
        {
            case CharacterClass characterClass ->
            {
                if (characterClass.getSource() != null
                        && StringUtils.hasText(characterClass.getSource().getAcronym()))
                {
                    sourceAcronym = characterClass.getSource().getAcronym();
                }
            }
            case Species species ->
            {
                if (species.getSource() != null
                        && StringUtils.hasText(species.getSource().getAcronym()))
                {
                    sourceAcronym = species.getSource().getAcronym();
                }
            }
            case Feat feat ->
            {
                if (feat.getSource() != null
                        && StringUtils.hasText(feat.getSource().getAcronym()))
                {
                    sourceAcronym = feat.getSource().getAcronym();
                }
            }
            default ->
            {
            }
        }

        if (sourceAcronym != null)
        {
            dto.setSource(sourceAcronym);
            dto.setName(name + " [" + sourceAcronym + "]");
        }
        else
        {
            dto.setName(name);
        }

        return dto;
    }

    default String joinSpellValues(List<?> values)
    {
        if (values == null || values.isEmpty())
        {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < values.size(); i++)
        {
            if (i > 0)
            {
                builder.append(" или ");
            }

            builder.append(values.get(i));
        }

        return builder.toString();
    }

    @Named("isConcentration")
    default Boolean isConcentration(Collection<SpellDuration> durations)
    {
        if (durations == null || durations.isEmpty())
        {
            return false;
        }

        return durations.stream()
                .map(SpellDuration::getConcentration)
                .anyMatch(Predicate.isEqual(true));
    }

    @Named("isRitual")
    default Boolean isRitual(Collection<SpellCastingTime> castingTimes)
    {
        if (castingTimes == null || castingTimes.isEmpty())
        {
            return false;
        }

        return castingTimes.stream()
                .map(SpellCastingTime::getUnit)
                .anyMatch(u -> CastingUnit.RITUAL == u);
    }

    @BaseMapping.BaseRequestNameMapping
    @Mapping(source = "source.url", target = "source.url")
    @Mapping(source = "sourcePage", target = "source.page")
    @Mapping(source = "school.school", target = "school")
    @Mapping(target = "affiliations", ignore = true)
    SpellRequest toRequest(Spell spell);

    @AfterMapping
    default void fillRequestFields(Spell spell, @MappingTarget SpellRequest target)
    {
        target.setAffiliations(buildCreateAffiliations(spell));
    }

    default CreateAffiliationRequest buildCreateAffiliations(Spell spell)
    {
        return CreateAffiliationRequest.builder()
                .species(extractUrls(spell.getSpeciesAffiliation()))
                .lineages(extractUrls(spell.getLineagesAffiliation()))
                .classes(extractUrls(spell.getClassAffiliation()))
                .subclasses(extractUrls(spell.getSubclassAffiliation()))
                .feats(extractUrls(spell.getFeatAffiliation()))
                .build();
    }

    default Set<String> extractUrls(Set<? extends NamedEntity> entities)
    {
        if (entities == null || entities.isEmpty())
        {
            return Set.of();
        }

        Set<String> result = new HashSet<>(entities.size());

        for (NamedEntity entity : entities)
        {
            if (entity != null && StringUtils.hasText(entity.getUrl()))
            {
                result.add(entity.getUrl());
            }
        }

        return result;
    }

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
    @Mapping(target = "featAffiliation", source = "feats")
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
