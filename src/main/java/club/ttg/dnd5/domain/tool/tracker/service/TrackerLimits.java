package club.ttg.dnd5.domain.tool.tracker.service;

import club.ttg.dnd5.domain.subscription.client.SubscriptionStatusClient;
import club.ttg.dnd5.domain.subscription.client.SubscriptionStatusClient.SubscriptionStatus;
import club.ttg.dnd5.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Сколько неудалённых трекеров может держать пользователь: 10 без подписки и 30 с действующей
 * подпиской.
 *
 * <p>Статус подписки берётся из subscriber-service на каждое создание, как у листов персонажа
 * и бастионов: роль в токене живёт до перелогина. Недоступность сервиса — не подписка
 * (fail-closed): уже созданные трекеры остаются, новый сверх базового лимита создать нельзя
 * до восстановления связи.</p>
 */
@Component
@RequiredArgsConstructor
public class TrackerLimits {
    /** Лимит без подписки. */
    public static final int BASE = 10;

    /** Лимит по действующей подписке. */
    public static final int SUBSCRIBER = 30;

    private final SubscriptionStatusClient subscriptionStatusClient;

    /** Лимит пользователя. */
    public int forUser(User user) {
        return subscriptionStatusClient.status(user.getUsername())
                .map(SubscriptionStatus::active)
                .orElse(false) ? SUBSCRIBER : BASE;
    }
}
