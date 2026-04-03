package club.ttg.dnd5.domain.common.rest.dto.notification;

import club.ttg.dnd5.domain.common.model.notification.NotificationType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDetailResponse {
    private Long id;
    private NotificationType type;
    private String typeName;
    private String personaId;
    private String text;
    private Long view;
    private LocalDateTime after;
    private LocalDateTime before;
    private boolean disabled;
    private String username;
    private LocalDateTime createdAt;
}

