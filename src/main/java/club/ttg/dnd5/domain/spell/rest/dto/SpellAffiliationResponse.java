package club.ttg.dnd5.domain.spell.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SpellAffiliationResponse {
    private Set<SpellAffiliationDto> classes;
    private Set<SpellAffiliationDto> subclasses;
    private Set<SpellAffiliationDto> species;
    private Set<SpellAffiliationDto> lineages;
    private Set<SpellAffiliationDto> feats;
}
