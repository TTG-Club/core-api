package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import club.ttg.dnd5.dto.base.serializer.FormattedMarkupDescriptionSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CreatureLairRequest {
    private String name;
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @JsonSerialize(using = FormattedMarkupDescriptionSerializer.class)
    private String description;
    private String original;
    private Collection<ActionRequest> effects;
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @JsonSerialize(using = FormattedMarkupDescriptionSerializer.class)
    private String ending;
}
