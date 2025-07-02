package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureLairResponse {
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String description;
    private Collection<ActionRequest> effects;
}
