package club.ttg.dnd5.domain.glossary.rest.dto;

import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GlossaryDetailedResponse extends GlossaryShortResponse{
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String description;

    private String alternative;
}
