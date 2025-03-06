package club.ttg.dnd5.domain.spell.mapper;


import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.rest.dto.SpellAffiliationDto;
import club.ttg.dnd5.domain.spell.rest.dto.SpellAffiliationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SpellAffiliationMapper {
    SpellAffiliationDto toSpellAffiliationDto(NamedEntity entity);

    @Mapping(target = "species", source = "speciesAffiliation")
    SpellAffiliationResponse toSpellAffiliationResponse(Spell spell);
}
