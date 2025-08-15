package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CreatureLairRequest {
    private String name;
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String description;
    private Collection<ActionRequest> effects;
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String ending;
}
