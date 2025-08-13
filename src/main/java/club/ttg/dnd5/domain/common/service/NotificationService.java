package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.common.repository.NotificationRepository;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationResponse getNotification() {
        return null;
    }
}
