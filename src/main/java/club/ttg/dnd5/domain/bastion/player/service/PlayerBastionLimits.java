package club.ttg.dnd5.domain.bastion.player.service;

import club.ttg.dnd5.domain.subscription.client.SubscriptionStatusClient;
import club.ttg.dnd5.domain.subscription.client.SubscriptionStatusClient.SubscriptionStatus;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.repository.UserRepository;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Сколько бастионов может создать пользователь: 3 без подписки и 12 с действующей подпиской.
 *
 * <p>Статус подписки берётся из subscriber-service на каждое создание, как у листов
 * персонажа: роль в токене живёт до перелогина. Недоступность сервиса — не подписка
 * (fail-closed): уже созданные бастионы остаются, новый сверх базового лимита создать нельзя
 * до восстановления связи.</p>
 */
@Component
@RequiredArgsConstructor
public class PlayerBastionLimits {
    /** Лимит без подписки. */
    public static final int BASE = 3;

    /** Лимит по действующей подписке. */
    public static final int SUBSCRIBER = 12;

    private final SubscriptionStatusClient subscriptionStatusClient;
    private final UserRepository userRepository;

    /** Лимит текущего пользователя. */
    public int forCurrentUser() {
        User user = SecurityUtils.getUser();
        return subscriptionStatusClient.status(user.getUsername())
                .map(SubscriptionStatus::active)
                .orElse(false) ? SUBSCRIBER : BASE;
    }

    /**
     * Есть ли у пользователя действующая подписка. Недоступность subscriber-service — не
     * подписка (fail-closed): платное не выдаётся, пока связь не восстановится.
     */
    public boolean isSubscriber(UUID userId) {
        return userRepository.findById(userId)
                .flatMap(user -> subscriptionStatusClient.status(user.getUsername()))
                .map(SubscriptionStatus::active)
                .orElse(false);
    }
}
