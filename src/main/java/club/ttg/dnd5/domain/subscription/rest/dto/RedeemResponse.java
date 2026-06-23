package club.ttg.dnd5.domain.subscription.rest.dto;

import club.ttg.dnd5.domain.subscription.model.RewardPerk;

import java.util.List;

/**
 * Итог погашения кода: созданная подписка (если код её нёс) и выданные награды.
 *
 * @param subscription  подписка в статусе REGISTERED; null, если код без подписки
 * @param grantedPerks  фактически выданные перки (без уже имевшихся у пользователя)
 */
public record RedeemResponse(
        SubscriptionResponse subscription,
        List<RewardPerk> grantedPerks
) {
}
