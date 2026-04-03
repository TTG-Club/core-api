package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.common.repository.NotificationRepository;
import club.ttg.dnd5.domain.common.repository.PersonaRepository;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationDetailResponse;
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

import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
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

        // Обновляем счетчик показов, если он задан (не бесконечный)
        if (notification.getView() != null && notification.getView() > 0) {
            notification.setView(notification.getView() - 1);
            if (notification.getView() == 0) {
                notification.setDisabled(true);
            }
            notificationRepository.save(notification);
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
        var notification = notificationRepository.findById(Long.valueOf(request.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Нотификация не найдена"));
        
        var persona = personaRepository.findById(UUID.fromString(request.getPersonaId()))
                .orElseThrow(() -> new EntityNotFoundException("Персона не найдена"));
                
        notificationMapper.updateEntity(request, notification);
        notification.setPersona(persona);
        
        return notificationRepository.save(notification).getId().toString();
    }

    public void delete(final Long id) {
        notificationRepository.deleteById(id);
    }

    public Collection<NotificationDetailResponse> getNotificationByPersona(final UUID personaId) {
        return personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException("Персона не найдена"))
                .getNotifications().stream()
                .map(notificationMapper::toDetailResponse)
                .toList();
    }
}
