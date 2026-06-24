package club.ttg.dnd5.domain.subscription.rest.dto;

import club.ttg.dnd5.domain.achievement.rest.dto.UserAchievementResponse;
import club.ttg.dnd5.domain.subscription.model.RewardPerk;

import java.util.List;

/**
 * Итог погашения кода: созданная подписка (если код её нёс), выданные перки и достижения.
 * Возвращает полный перечень начисленного, чтобы фронт показал модалку «что получено».
 *
 * @param subscription        подписка в статусе REGISTERED; null, если код без подписки
 * @param grantedPerks        фактически выданные перки (без уже имевшихся у пользователя)
 * @param grantedAchievements фактически выданные достижения (без уже имевшихся)
 */
public record RedeemResponse(
        SubscriptionResponse subscription,
        List<RewardPerk> grantedPerks,
        List<UserAchievementResponse> grantedAchievements
) {
}
