package club.ttg.dnd5.domain.common.rest.controller;

import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationDetailResponse;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;

import java.util.Collection;
import java.util.UUID;

@Tag(name = "Нотификации на главной", description = "API для нотификаций")
@RequiredArgsConstructor
@RequestMapping("/api/v2/notification")
@RestController
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "Получение случайной нотификации")
    @GetMapping
    public NotificationResponse getNotification(@RequestHeader(value = "X-Guest-Id", required = false) String guestId) {
        return notificationService.getNotification(guestId);
    }

    @Operation(summary = "Получение нотификаций по ID персоны (полная модель для админки)")
    @GetMapping("/persona/{id}")
    public Collection<NotificationDetailResponse> getNotificationsByPersona(@PathVariable("id") UUID personaId) {
        return notificationService.getNotificationByPersona(personaId);
    }

    @Operation(summary = "Создание новой нотификации")
    @PostMapping
    public String save(@RequestBody NotificationRequest request) {
        return notificationService.save(request);
    }

    @Operation(summary = "Обновление существующей нотификации")
    @PutMapping
    public String update(@RequestBody NotificationRequest request) {
        return notificationService.update(request);
    }

    @Operation(summary = "Удаление нотификации по ID")
    @DeleteMapping
    public void delete(@RequestParam Long id) {
        notificationService.delete(id);
    }
}
