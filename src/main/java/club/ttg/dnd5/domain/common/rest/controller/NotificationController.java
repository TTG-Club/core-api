package club.ttg.dnd5.domain.common.rest.controller;

import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationRequest;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationResponse;
import club.ttg.dnd5.domain.common.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Нотификации на главной", description = "API для нотификаций")
@RequiredArgsConstructor
@RequestMapping("/api/v2/notification")
@RestController
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public NotificationResponse getNotification() {
        return notificationService.getNotification();
    }
    @PostMapping
    String save(NotificationRequest request) {
        return notificationService.save(request);
    }
}
