package club.ttg.dnd5.domain.spell.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
    private String castingTime;
    private String range;
    private String duration;
    private SpellDetailedComponents components;
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String description;
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String upper;
    private SpellAffiliationResponse affiliation;
}
