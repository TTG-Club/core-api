package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.common.model.notification.Notification;
import club.ttg.dnd5.domain.common.model.notification.NotificationType;
import club.ttg.dnd5.domain.common.model.notification.NotificationView;
import club.ttg.dnd5.domain.common.repository.NotificationRepository;
import club.ttg.dnd5.domain.common.repository.NotificationViewRepository;
import club.ttg.dnd5.domain.common.repository.PersonaRepository;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationDetailResponse;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationRequest;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationResponse;
import club.ttg.dnd5.domain.common.rest.mapper.NotificationMapper;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Service
public class NotificationService {
    private static final String ANONYMOUS_VIEWER_KEY = "anonymous";
    private static final String GUEST_VIEWER_PREFIX = "guest:";
    private static final String USER_VIEWER_PREFIX = "user:";

    private final NotificationRepository notificationRepository;
    private final NotificationViewRepository notificationViewRepository;
    private final PersonaRepository personaRepository;
    private final NotificationMapper notificationMapper;

    @Value("${notification.advertising-weight:5}")
    private int advertisingWeight;

    @Transactional
    public NotificationResponse getNotification(String guestId) {
        var now = LocalDateTime.now();
        var notifications = notificationRepository.findEligible(now);
        if (notifications.isEmpty()) {
            return null;
        }

        var viewerKey = getViewerKey(guestId);
        var lastNotificationId = notificationViewRepository.findFirstByUsernameOrderByViewedAtDescIdDesc(viewerKey)
                .map(view -> view.getNotification().getId())
                .orElse(null);
        var viewedNotificationIds = findViewedNotificationIds(viewerKey, notifications);
        var notification = selectNotification(notifications, viewedNotificationIds, lastNotificationId);
        if (notification == null) {
            return null;
        }

        if (notification.getView() != null && notification.getView() > 0) {
            notification.setView(notification.getView() - 1);
            if (notification.getView() == 0) {
                notification.setDisabled(true);
            }
            notificationRepository.save(notification);
        }

        saveNotificationView(notification, viewerKey);

        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public String save(final NotificationRequest request) {
        var persona = personaRepository.findById(UUID.fromString(request.getPersonaId()))
                .orElseThrow(() -> new EntityNotFoundException("Персона не найдена"));
        var notification = notificationMapper.toEntity(request);
        notification.setPersona(persona);
        return notificationRepository.save(notification).getId().toString();
    }

    @Transactional
    public String update(final NotificationRequest request) {
        var notification = notificationRepository.findById(Long.valueOf(request.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Нотификация не найдена"));

        var persona = personaRepository.findById(UUID.fromString(request.getPersonaId()))
                .orElseThrow(() -> new EntityNotFoundException("Персона не найдена"));

        notificationMapper.updateEntity(request, notification);
        notification.setPersona(persona);

        return notificationRepository.save(notification).getId().toString();
    }

    @Transactional
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

    private Notification selectNotification(
            List<Notification> notifications,
            Set<Long> viewedNotificationIds,
            Long lastNotificationId
    ) {
        var visibleNotifications = notifications.stream()
                .filter(notification -> notification.getType() != NotificationType.NEWS
                        || !viewedNotificationIds.contains(notification.getId()))
                .toList();
        if (visibleNotifications.isEmpty()) {
            return null;
        }

        var unreadNews = withoutLastNotification(
                visibleNotifications.stream()
                        .filter(notification -> notification.getType() == NotificationType.NEWS)
                        .toList(),
                lastNotificationId
        );

        if (!unreadNews.isEmpty()) {
            return unreadNews.stream()
                    .max(Comparator.comparing(Notification::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(Notification::getId))
                    .orElseThrow();
        }

        var candidates = withoutLastNotification(
                visibleNotifications.stream()
                        .filter(notification -> notification.getType() != NotificationType.NEWS)
                        .toList(),
                lastNotificationId
        );

        if (candidates.isEmpty()) {
            candidates = visibleNotifications.stream()
                    .filter(notification -> notification.getType() != NotificationType.NEWS)
                    .toList();
        }

        if (candidates.isEmpty()) {
            return null;
        }

        return selectWeighted(candidates);
    }

    private List<Notification> withoutLastNotification(List<Notification> notifications, Long lastNotificationId) {
        if (notifications.size() <= 1 || lastNotificationId == null) {
            return notifications;
        }

        return notifications.stream()
                .filter(notification -> !notification.getId().equals(lastNotificationId))
                .toList();
    }

    private Notification selectWeighted(List<Notification> notifications) {
        var totalWeight = notifications.stream()
                .mapToInt(this::getWeight)
                .sum();
        var selectedWeight = ThreadLocalRandom.current().nextInt(totalWeight);

        for (Notification notification : notifications) {
            selectedWeight -= getWeight(notification);
            if (selectedWeight < 0) {
                return notification;
            }
        }

        return notifications.getLast();
    }

    private int getWeight(Notification notification) {
        return notification.getType() == NotificationType.ADVERTISING && notification.getView() != null
                ? advertisingWeight
                : 1;
    }

    private Set<Long> findViewedNotificationIds(String username, List<Notification> notifications) {
        var notificationIds = notifications.stream()
                .map(Notification::getId)
                .toList();

        return notificationViewRepository.findViewedNotificationIds(username, notificationIds);
    }

    private void saveNotificationView(Notification notification, String username) {
        var view = new NotificationView();
        view.setNotification(notification);
        view.setUsername(username);
        notificationViewRepository.save(view);
    }

    private String getViewerKey(String guestId) {
        return getCurrentUsername()
                .map(username -> USER_VIEWER_PREFIX + username)
                .orElseGet(() -> StringUtils.hasText(guestId)
                        ? GUEST_VIEWER_PREFIX + guestId.trim()
                        : ANONYMOUS_VIEWER_KEY);
    }

    private Optional<String> getCurrentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        return Optional.ofNullable(authentication.getName());
    }
}
