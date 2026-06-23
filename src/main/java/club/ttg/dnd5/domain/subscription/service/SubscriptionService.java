package club.ttg.dnd5.domain.subscription.service;

import club.ttg.dnd5.domain.subscription.model.SubscriptionType;
import club.ttg.dnd5.domain.subscription.model.UserSubscription;
import club.ttg.dnd5.domain.subscription.repository.UserSubscriptionRepository;
import club.ttg.dnd5.domain.subscription.rest.dto.SubscriptionResponse;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private static final char[] CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int CODE_LENGTH = 16;
    private static final int MAX_CODE_ATTEMPTS = 20;

    private final UserSubscriptionRepository repository;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public SubscriptionResponse createGift(int durationMonths) {
        if (durationMonths < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Срок подписки должен быть не меньше 1 месяца");
        }

        UserSubscription subscription = new UserSubscription();
        subscription.setType(SubscriptionType.GIFT);
        subscription.setDurationMonths(durationMonths);
        subscription.setRegistrationCode(generateUniqueCode());
        return toResponse(repository.save(subscription), Instant.now());
    }

    @Transactional
    public SubscriptionResponse register(String code) {
        String username = currentUsername();
        UserSubscription subscription = repository.findByRegistrationCode(normalizeCode(code))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Код подписки не найден"));

        if (subscription.getOwnerUsername() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Подписка уже зарегистрирована");
        }

        subscription.setOwnerUsername(username);
        subscription.setRegisteredAt(Instant.now());
        return toResponse(subscription, Instant.now());
    }

    @Transactional
    public SubscriptionResponse activate(UUID id) {
        String username = currentUsername();
        UserSubscription subscription = repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Подписка не найдена"));

        if (!username.equals(subscription.getOwnerUsername())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Подписка зарегистрирована на другого пользователя");
        }
        if (subscription.getStartsAt() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Подписка уже активирована");
        }

        Instant now = Instant.now();
        subscription.setStartsAt(now);
        subscription.setExpiresAt(ZonedDateTime.ofInstant(now, ZoneOffset.UTC)
                .plusMonths(subscription.getDurationMonths())
                .toInstant());
        return toResponse(subscription, now);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> currentUserSubscriptions() {
        Instant now = Instant.now();
        return repository.findByOwnerUsernameOrderByCreatedAtDesc(currentUsername()).stream()
                .map(subscription -> toResponse(subscription, now))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasRegisteredSubscription(String username) {
        return StringUtils.hasText(username) && repository.existsByOwnerUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(String username, Instant now) {
        return StringUtils.hasText(username)
                && repository.existsByOwnerUsernameAndStartsAtIsNotNullAndExpiresAtAfter(username, now);
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
            String code = randomCode();
            if (!repository.existsByRegistrationCode(code)) {
                return code;
            }
        }
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось создать уникальный код подписки");
    }

    private String randomCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(CODE_ALPHABET[random.nextInt(CODE_ALPHABET.length)]);
        }
        return builder.toString();
    }

    private String currentUsername() {
        String username = SecurityUtils.getUser().getUsername();
        if (!StringUtils.hasText(username)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        return username;
    }

    private String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Код подписки обязателен");
        }
        String normalized = code.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (!StringUtils.hasText(normalized)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Код подписки обязателен");
        }
        return normalized;
    }

    private SubscriptionResponse toResponse(UserSubscription subscription, Instant now) {
        return new SubscriptionResponse(
                subscription.getUuid(),
                subscription.getType(),
                status(subscription, now),
                subscription.getRegistrationCode(),
                subscription.getDurationMonths(),
                subscription.getOwnerUsername(),
                subscription.getRegisteredAt(),
                subscription.getStartsAt(),
                subscription.getExpiresAt(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt());
    }

    private String status(UserSubscription subscription, Instant now) {
        if (subscription.getOwnerUsername() == null) {
            return "CREATED";
        }
        if (subscription.getStartsAt() == null) {
            return "REGISTERED";
        }
        if (subscription.getExpiresAt() != null && subscription.getExpiresAt().isAfter(now)) {
            return "ACTIVE";
        }
        return "EXPIRED";
    }
}
