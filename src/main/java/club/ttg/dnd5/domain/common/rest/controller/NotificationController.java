package club.ttg.dnd5.domain.common.rest.controller;

import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationRequest;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationResponse;
import club.ttg.dnd5.domain.common.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.UUID;

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

    @GetMapping("/persona/{id}")
    public Collection<NotificationResponse> getNotificationsByPersona(@PathVariable("id") UUID personaId) {
        return notificationService.getNotificationByPersona(personaId);
    }

    @PostMapping
    public String save(NotificationRequest request) {
        return notificationService.save(request);
    }

    @PutMapping
    public String update(NotificationRequest request) {
        return notificationService.update(request);
    }

    @DeleteMapping
    public void delete(UUID id) {
        notificationService.delete(id);
    }
}
