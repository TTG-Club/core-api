package club.ttg.dnd5.domain.common.rest.dto.notification;

import club.ttg.dnd5.domain.common.model.notification.NotificationType;
import lombok.Getter;

@Getter
public class NotificationTypeResponse {
    private final String value;
    private final String name;

    public NotificationTypeResponse(NotificationType type) {
        this.value = type.name();
        this.name = type.getName();
    }
}
