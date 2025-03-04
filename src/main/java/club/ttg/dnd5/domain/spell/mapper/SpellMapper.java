package club.ttg.dnd5.domain.spell.mapper;

import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.model.SpellCastingTime;
import club.ttg.dnd5.domain.spell.model.SpellDistance;
import club.ttg.dnd5.domain.spell.model.SpellDuration;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import jdk.jfr.Name;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(uses = {SpellComponentsMapper.class}, componentModel = "spring")
public interface SpellMapper {

    @Mapping(target = "school", source = "spell.school.school.name")
    @Mapping(target = "additionalType", source = "spell.school.additionalType")
    @Mapping(target = "name", source = "spell")
    SpellShortResponse toSpeciesShortResponse(Spell spell);

    @Mapping(target = "school", source = "spell.school.school.name")
    @Mapping(target = "additionalType", source = "spell.school.additionalType")
    @Mapping(target = "name", source = "spell")
    @Mapping(target = "castingTime", source = "spell", qualifiedByName = "castingTimeToString")
    @Mapping(target = "duration", source = "spell", qualifiedByName = "durationToString")
    @Mapping(target = "range", source = "spell", qualifiedByName = "distanceToString")
    SpellDetailedResponse toSpellDetailedResponse(Spell spell);

    NameResponse toNameResponse(Spell spell);

    @Named("castingTimeToString")
    default String castingTimeToString(Spell spell) {
        String castingTime = spell.getCastingTime().stream()
                .map(SpellCastingTime::toString)
                .collect(Collectors.joining(", "));
        String customCastingTime = spell.getCustomCastingTime();
        return Objects.nonNull(customCastingTime) ? String.format("%s или %s", castingTime, customCastingTime) : castingTime;
    }

    @Named("durationToString")
    default String durationToString(Spell spell) {
        String duration = spell.getDuration().stream()
                .map(SpellDuration::toString)
                .collect(Collectors.joining(", "));
        String customDuration = spell.getCustomDuration();
        return Objects.nonNull(customDuration) ? String.format("%s или %s", duration, customDuration) : duration;
    }

    @Named("distanceToString")
    default String distanceToString(Spell spell) {
        String distance = spell.getDistance().stream()
                .map(SpellDistance::toString)
                .collect(Collectors.joining(", "));
        String customDistance = spell.getCustomDistance();
        return Objects.nonNull(customDistance) ? String.format("%s или %s", distance, customDistance) : distance;
    }
}
