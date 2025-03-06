package club.ttg.dnd5.domain.spell.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class SpellDetailedResponse extends ShortResponse {
    private Long level;
    private String school;
    private String additionalType;
    private Boolean concentration;
    private Boolean ritual;
    private String castingTime;
    private String range;
    private String duration;
    private SpellDetailedComponents components;
    private String description;
    private String upper;
    private SpellAffiliationResponse affiliation;
}
