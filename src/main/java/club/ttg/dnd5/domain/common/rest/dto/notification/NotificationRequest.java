package club.ttg.dnd5.domain.common.rest.dto.notification;

import club.ttg.dnd5.domain.common.model.notification.NotificationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {
    private String id;
    private NotificationType type;
    private String personAId;
    private String text;
    private String before;
    private String after;
    private boolean disabled;
    private Long view;
}
