package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.common.repository.NotificationRepository;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationRequest;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationResponse;
import club.ttg.dnd5.domain.common.rest.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class NotificationService {
    private static final Random RND = new Random();

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationResponse getNotification() {
        var now = LocalDateTime.now();
        long total = notificationRepository.countEligible(now);
        if (total <= 0) {
            return null; // нет подходящих уведомлений
        }

        int index = RND.nextInt(Math.toIntExact(total));
        var page = notificationRepository.findEligible(now, PageRequest.of(index, 1));
        var notification = page.stream().findFirst().orElse(null);
        if (notification == null) {
            return null;
        }
        return notificationMapper.toResponse(notification);
    }

    public String save(final NotificationRequest request) {
        notificationMapper.toEntity(request);
        return null;
    }
}
