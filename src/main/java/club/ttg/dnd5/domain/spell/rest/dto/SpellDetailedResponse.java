package club.ttg.dnd5.domain.spell.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String description;
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String upper;
    private SpellAffiliationResponse affiliation;
}
