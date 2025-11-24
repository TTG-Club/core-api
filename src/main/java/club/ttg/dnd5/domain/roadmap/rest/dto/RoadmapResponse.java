package club.ttg.dnd5.domain.roadmap.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.rating.RatingResponse;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoadmapResponse {
    private String url;
    private String name;
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String description;
    private RatingResponse rate;
}
