package club.ttg.dnd5.domain.spell.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SpellAffiliationResponse {
    private List<SpellAffiliationDto> classes;
    private List<SpellAffiliationDto> subclasses;
    private List<SpellAffiliationDto> species;
    private List<SpellAffiliationDto> lineages;
}
