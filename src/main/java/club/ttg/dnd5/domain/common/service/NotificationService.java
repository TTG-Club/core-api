package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.common.repository.NotificationRepository;
import club.ttg.dnd5.domain.common.repository.PersonaRepository;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationRequest;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationResponse;
import club.ttg.dnd5.domain.common.rest.mapper.NotificationMapper;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class NotificationService {
    private static final Random RND = new Random();

    private final NotificationRepository notificationRepository;
    private final PersonaRepository personaRepository;
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
        var persona = personaRepository.findById(UUID.fromString(request.getPersonaId()))
                .orElseThrow(() -> new EntityNotFoundException("Персона не найдена"));
        var notification = notificationMapper.toEntity(request);
        notification.setPersona(persona);
        return notificationRepository.save(notification).getId().toString();
    }

    public String update(final NotificationRequest request) {
        var persona = personaRepository.findById(UUID.fromString(request.getPersonaId()))
                .orElseThrow(() -> new EntityNotFoundException("Персона не найдена"));
        var notification = notificationMapper.toEntity(request);
        notification.setPersona(persona);
        return notificationRepository.save(notification).getId().toString();
    }

    public void delete(final UUID id) {
        notificationRepository.deleteById(id);
    }

    public Collection<NotificationResponse> getNotificationByPersona(final UUID personaId) {
        return personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException("Персона не найдена"))
                .getNotifications().stream()
                .map(notificationMapper::toResponse)
                .toList();
    }
}
