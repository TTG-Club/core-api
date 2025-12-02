package club.ttg.dnd5.domain.roadmap.rest.dto;

import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoadmapRequest {
    private String url;
    private String name;
    private String preview;
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String description;
    private boolean visible;
}
