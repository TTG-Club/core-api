package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeciesInnateSpellResponse
{
    private SpellShortResponse spell;
    private Integer requiredLevel;
}
