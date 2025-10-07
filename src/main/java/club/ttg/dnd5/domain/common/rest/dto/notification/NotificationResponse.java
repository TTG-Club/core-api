package club.ttg.dnd5.domain.common.rest.dto.notification;

import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationResponse {
    private String persona;
    private String image;
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String text;
}
