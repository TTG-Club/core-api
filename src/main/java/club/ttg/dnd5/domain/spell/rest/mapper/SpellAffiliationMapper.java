package club.ttg.dnd5.domain.spell.rest.mapper;


import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.rest.dto.SpellAffiliationDto;
import club.ttg.dnd5.domain.spell.rest.dto.SpellAffiliationResponse;
import club.ttg.dnd5.domain.spell.rest.dto.create.CreateAffiliationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SpellAffiliationMapper {
    default SpellAffiliationDto toSpellAffiliationDto(NamedEntity entity) {
        if (entity == null) {
            return null;
        }

        SpellAffiliationDto dto = new SpellAffiliationDto();
        dto.setUrl(entity.getUrl());

        String name = entity.getName();
        String sourceAcronym = null;

        if (entity instanceof CharacterClass characterClass) {
            if (characterClass.getSource() != null && characterClass.getSource().getAcronym() != null) {
                sourceAcronym = "[" + characterClass.getSource().getAcronym() + "]";
            }
        } else if (entity instanceof Species species) {
            if (species.getSource() != null && species.getSource().getAcronym() != null) {
                sourceAcronym = "[" + species.getSource().getAcronym() + "]";
            }
        }

        if (sourceAcronym != null) {
            dto.setName(name + " " + sourceAcronym);
        } else {
            dto.setName(name);
        }

        return dto;
    }

    @Mapping(target = "classes", source = "classAffiliation")
    @Mapping(target = "subclasses", source = "subclassAffiliation")
    @Mapping(target = "species", source = "speciesAffiliation")
    @Mapping(target = "lineages", source = "lineagesAffiliation")
    SpellAffiliationResponse toSpellAffiliationResponse(Spell spell);

    default CreateAffiliationRequest toCreateAffiliationRequest(Spell spell) {
        return CreateAffiliationRequest.builder()
                .species(spell.getSpeciesAffiliation().stream()
                        .map(NamedEntity::getUrl)
                        .collect(Collectors.toList()))
                .lineages(spell.getLineagesAffiliation().stream()
                        .map(NamedEntity::getUrl)
                        .collect(Collectors.toList()))
                .classes(spell.getClassAffiliation().stream()
                        .map(NamedEntity::getUrl)
                        .collect(Collectors.toList()))
                .subclasses(spell.getSubclassAffiliation().stream()
                        .map(NamedEntity::getUrl)
                        .collect(Collectors.toList()))
                .build();
    }
}
