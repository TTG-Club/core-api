package club.ttg.dnd5.domain.subscription.service;

import club.ttg.dnd5.domain.achievement.rest.dto.UserAchievementResponse;
import club.ttg.dnd5.domain.achievement.service.AchievementService;
import club.ttg.dnd5.domain.subscription.model.RedemptionCode;
import club.ttg.dnd5.domain.subscription.model.RewardPerk;
import club.ttg.dnd5.domain.subscription.model.RewardResource;
import club.ttg.dnd5.domain.subscription.model.UserSubscription;
import club.ttg.dnd5.domain.subscription.repository.RedemptionCodeRepository;
import club.ttg.dnd5.domain.subscription.repository.UserSubscriptionRepository;
import club.ttg.dnd5.domain.subscription.rest.dto.CreateCodesRequest;
import club.ttg.dnd5.domain.subscription.rest.dto.MyRedemptionResponse;
import club.ttg.dnd5.domain.subscription.rest.dto.RedeemResponse;
import club.ttg.dnd5.domain.subscription.rest.dto.RedemptionCodeResponse;
import club.ttg.dnd5.domain.subscription.rest.dto.SubscriptionResponse;
import club.ttg.dnd5.domain.user.model.Role;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.repository.RoleRepository;
import club.ttg.dnd5.domain.user.repository.UserRepository;
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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    /** Роль, выдаваемая при активации подписки и снимаемая по её истечении. */
    public static final String SUBSCRIBER_ROLE = "SUBSCRIBER";

    private static final char[] CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int CODE_LENGTH = 16;
    private static final int MAX_CODE_ATTEMPTS = 20;
    private static final int MAX_BATCH_SIZE = 1000;

    private final RedemptionCodeRepository codeRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final RewardService rewardService;
    private final AchievementService achievementService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SecureRandom random = new SecureRandom();

    /**
     * Выпускает пачку одноразовых кодов с одинаковым содержимым.
     */
    @Transactional
    public List<RedemptionCodeResponse> createCodes(CreateCodesRequest request) {
        int count = request.count() == null ? 1 : request.count();
        if (count < 1 || count > MAX_BATCH_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Количество кодов должно быть от 1 до " + MAX_BATCH_SIZE);
        }
        boolean hasPerks = request.perks() != null && !request.perks().isEmpty();
        boolean hasAchievements = request.achievements() != null && !request.achievements().isEmpty();
        if (request.subscriptionMonths() == null && request.rewardTier() == null && !hasPerks && !hasAchievements) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Код должен нести подписку, тир, перки или достижения");
        }
        if (request.subscriptionMonths() != null) {
            if (request.subscriptionMonths() < 1) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Срок подписки должен быть не меньше 1 месяца");
            }
            if (request.subscriptionType() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Не указан тип подписки");
            }
        }

        Set<String> batch = new HashSet<>();
        List<RedemptionCodeResponse> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            RedemptionCode code = new RedemptionCode();
            code.setCode(generateUniqueCode(batch));
            code.setSubscriptionType(request.subscriptionMonths() == null ? null : request.subscriptionType());
            code.setSubscriptionMonths(request.subscriptionMonths());
            code.setRewardTier(request.rewardTier());
            code.setPerks(hasPerks ? EnumSet.copyOf(request.perks()) : new HashSet<>());
            code.setAchievements(hasAchievements ? new HashSet<>(request.achievements()) : new HashSet<>());
            code.setLabel(request.label());
            result.add(toResponse(codeRepository.save(code)));
        }
        return result;
    }

    /**
     * Погашает код: выдаёт перки и достижения сразу (навсегда) и создаёт подписку
     * в статусе REGISTERED, если код её нёс. Таймер запускается отдельно — {@link #activate}.
     * <p>
     * Захват кода атомарен ({@link RedemptionCodeRepository#claim}): при гонке двух
     * параллельных погашений ровно одно обновит строку, второе получит 409.
     */
    @Transactional
    public RedeemResponse redeem(String rawCode) {
        String username = currentUsername();
        String normalized = normalizeCode(rawCode);
        RedemptionCode code = codeRepository.findByCode(normalized)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Код не найден"));
        if (code.getRedeemedBy() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "Код уже использован");
        }
        if (code.isDisabled()) {
            throw new ApiException(HttpStatus.CONFLICT, "Код деактивирован");
        }

        Instant now = Instant.now();
        if (codeRepository.claim(normalized, username, now) == 0) {
            // строку успел захватить параллельный запрос между findByCode и claim
            throw new ApiException(HttpStatus.CONFLICT, "Код уже использован");
        }

        SubscriptionResponse subscription = null;
        if (code.getSubscriptionMonths() != null) {
            UserSubscription entity = new UserSubscription();
            entity.setType(code.getSubscriptionType());
            entity.setDurationMonths(code.getSubscriptionMonths());
            entity.setOwnerUsername(username);
            entity.setSourceCode(code.getUuid());
            entity.setRegisteredAt(now);
            subscription = toResponse(subscriptionRepository.save(entity), now);
        }

        // перки = пресет тира ∪ произвольный набор кода
        Set<RewardPerk> perks = EnumSet.noneOf(RewardPerk.class);
        if (code.getRewardTier() != null) {
            perks.addAll(code.getRewardTier().perks());
        }
        perks.addAll(code.getPerks());
        List<RewardPerk> grantedPerks = rewardService.grantPerks(username, perks, code.getUuid());

        List<UserAchievementResponse> grantedAchievements =
                achievementService.grant(username, code.getAchievements(), code.getUuid(), null);

        return new RedeemResponse(subscription, grantedPerks, grantedAchievements);
    }

    /**
     * Активирует накопленную подписку пользователя: фиксирует даты старта/окончания
     * и выдаёт роль {@link #SUBSCRIBER_ROLE}.
     */
    @Transactional
    public SubscriptionResponse activate(UUID id) {
        String username = currentUsername();
        UserSubscription subscription = subscriptionRepository.findById(id)
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

        grantSubscriberRole(username);
        return toResponse(subscription, now);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> allSubscriptions() {
        Instant now = Instant.now();
        return subscriptionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(subscription -> toResponse(subscription, now))
                .toList();
    }

    /** Все выпущенные коды для админского списка (новые сверху). */
    @Transactional(readOnly = true)
    public List<RedemptionCodeResponse> allCodes() {
        return codeRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Мягко деактивирует или возвращает в строй выпущенный код. Деактивированный код
     * нельзя погасить, но запись сохраняется. Менять статус уже использованного кода
     * нельзя. Деактивацию пишем в аудит (кем/когда), при возврате — очищаем.
     */
    @Transactional
    public RedemptionCodeResponse setCodeDisabled(UUID id, boolean disabled) {
        RedemptionCode code = codeRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Код не найден"));
        if (code.getRedeemedBy() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Нельзя изменить статус уже использованного кода");
        }

        if (disabled) {
            code.setDisabled(true);
            code.setDisabledAt(Instant.now());
            code.setDisabledBy(currentUsername());
        } else {
            code.setDisabled(false);
            code.setDisabledAt(null);
            code.setDisabledBy(null);
        }
        return toResponse(codeRepository.save(code));
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> currentUserSubscriptions() {
        Instant now = Instant.now();
        return subscriptionRepository.findByOwnerUsernameOrderByCreatedAtDesc(currentUsername()).stream()
                .map(subscription -> toResponse(subscription, now))
                .toList();
    }

    /**
     * Коды, погашенные текущим пользователем (новые сверху), с резолвом наград в
     * ссылки и привязанной подпиской — для раздела «Активация кодов» в кабинете.
     * Награды кода = пресет тира ∪ доп. перки кода (как при погашении), что даёт
     * стабильный перечень ссылок независимо от дедупликации выданных перков.
     */
    @Transactional(readOnly = true)
    public List<MyRedemptionResponse> currentUserRedemptions() {
        String username = currentUsername();
        Instant now = Instant.now();

        // подписки пользователя по коду-источнику — чтобы прицепить к своей строке без N+1
        Map<UUID, UserSubscription> subscriptionsByCode = new HashMap<>();
        for (UserSubscription subscription : subscriptionRepository.findByOwnerUsernameOrderByCreatedAtDesc(username)) {
            if (subscription.getSourceCode() != null) {
                subscriptionsByCode.putIfAbsent(subscription.getSourceCode(), subscription);
            }
        }

        // справочник ссылок на награды — один раз на весь список (а не на каждый код)
        Map<RewardPerk, RewardResource> resources = rewardService.resourceMap();

        return codeRepository.findByRedeemedByOrderByRedeemedAtDesc(username).stream()
                .map(code -> {
                    Set<RewardPerk> perks = EnumSet.noneOf(RewardPerk.class);
                    if (code.getRewardTier() != null) {
                        perks.addAll(code.getRewardTier().perks());
                    }
                    perks.addAll(code.getPerks());

                    UserSubscription subscription = subscriptionsByCode.get(code.getUuid());
                    return new MyRedemptionResponse(
                            code.getUuid(),
                            code.getCode(),
                            code.getRedeemedAt(),
                            code.getRewardTier(),
                            rewardService.describe(perks, code.getRedeemedAt(), resources),
                            achievementService.describe(code.getAchievements(), code.getRedeemedAt()),
                            subscription == null ? null : toResponse(subscription, now));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasRegisteredSubscription(String username) {
        return StringUtils.hasText(username) && subscriptionRepository.existsByOwnerUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(String username, Instant now) {
        return StringUtils.hasText(username)
                && subscriptionRepository.existsByOwnerUsernameAndStartsAtIsNotNullAndExpiresAtAfter(username, now);
    }

    /** Добавляет пользователю роль подписчика, если её ещё нет. */
    private void grantSubscriberRole(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        boolean alreadyHas = user.getRoles() != null && user.getRoles().stream()
                .anyMatch(role -> SUBSCRIBER_ROLE.equals(role.getName()));
        if (alreadyHas) {
            return;
        }
        Role subscriberRole = roleRepository.findByName(SUBSCRIBER_ROLE);
        if (subscriberRole == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Роль " + SUBSCRIBER_ROLE + " не сконфигурирована");
        }
        List<Role> roles = user.getRoles() == null ? new ArrayList<>() : new ArrayList<>(user.getRoles());
        roles.add(subscriberRole);
        user.setRoles(roles);
        userRepository.save(user);
    }

    private String generateUniqueCode(Set<String> batch) {
        for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
            String code = randomCode();
            if (batch.add(code) && !codeRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось создать уникальный код");
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
            throw new ApiException(HttpStatus.BAD_REQUEST, "Код обязателен");
        }
        String normalized = code.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (!StringUtils.hasText(normalized)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Код обязателен");
        }
        return normalized;
    }

    private RedemptionCodeResponse toResponse(RedemptionCode code) {
        return new RedemptionCodeResponse(
                code.getUuid(),
                code.getCode(),
                code.getSubscriptionType(),
                code.getSubscriptionMonths(),
                code.getRewardTier(),
                code.getPerks(),
                code.getAchievements(),
                code.getLabel(),
                code.getRedeemedBy(),
                code.getRedeemedAt(),
                code.isDisabled(),
                code.getDisabledAt(),
                code.getDisabledBy(),
                code.getCreatedAt());
    }

    private SubscriptionResponse toResponse(UserSubscription subscription, Instant now) {
        return new SubscriptionResponse(
                subscription.getUuid(),
                subscription.getType(),
                status(subscription, now),
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
