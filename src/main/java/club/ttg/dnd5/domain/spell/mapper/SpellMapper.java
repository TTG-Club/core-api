package club.ttg.dnd5.domain.spell.mapper;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.clazz.model.ClassCharacter;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.model.SpellCastingTime;
import club.ttg.dnd5.domain.spell.model.SpellDistance;
import club.ttg.dnd5.domain.spell.model.SpellDuration;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.dto.create.SpellRequest;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(uses = {SpellComponentsMapper.class, SpellAffiliationMapper.class, BaseMapping.class}, componentModel = "spring")
public interface SpellMapper {

    @BaseMapping.BaseEntityNameMapping
    @Mapping(target = "url", source = "request.url")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "sourcePage", source = "request.source.page")
    @Mapping(target = "school.school", source = "request.school")
    @Mapping(target = "ritual", source = "request.ritual")
    @Mapping(target = "concentration", source = "request.concentration")
    @Mapping(target = "components", source = "request.components")
    @Mapping(target = "range", source = "request.range")
    @Mapping(target = "castingTime", source = "request.castingTime")
    @Mapping(target = "duration", source = "request.duration")
    @Mapping(target = "upper", source = "request.upper")

    @Mapping(target = "source", source = "source")
    @Mapping(target = "speciesAffiliation", source = "species")
    @Mapping(target = "updatedAt", ignore = true)
    Spell toEntity(SpellRequest request, Book source,
                   List<ClassCharacter> classes, List<ClassCharacter> subclasses,
                   List<Species> species, List<Species> lineages);

    @Mapping(target = "school", source = "school.school.name")
    @Mapping(target = "additionalType", source = "school.additionalType")
    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    SpellShortResponse toSpeciesShortResponse(Spell spell);


    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping

    @Mapping(target = "school", source = "school.school.name")
    @Mapping(target = "additionalType", source = "school.additionalType")
    @Mapping(target = "castingTime", source = ".", qualifiedByName = "castingTimeToString")
    @Mapping(target = "duration", source = ".", qualifiedByName = "durationToString")
    @Mapping(target = "range", source = ".", qualifiedByName = "distanceToString")
    @Mapping(target = "affiliation", source = ".")
    SpellDetailedResponse toSpellDetailedResponse(Spell spell);


    @Named("castingTimeToString")
    default String castingTimeToString(Spell spell) {
        return  spell.getCastingTime().stream()
                .map(SpellCastingTime::toString)
                .collect(Collectors.joining(", "));
         }

    @Named("durationToString")
    default String durationToString(Spell spell) {
         return spell.getDuration().stream()
                .map(SpellDuration::toString)
                .collect(Collectors.joining(", "));
    }

    @Named("distanceToString")
    default String distanceToString(Spell spell) {
        return spell.getRange().stream()
                .map(SpellDistance::toString)
                .collect(Collectors.joining(", "));
    }
}
