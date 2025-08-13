package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CreatureLairResponse {
    private String name;
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String description;
    private Collection<ActionResponse> effects;
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String ending;
}
