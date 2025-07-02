package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureLairRequest {
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String description;
    private Collection<ActionRequest> effects;
}
