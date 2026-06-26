package club.ttg.dnd5.domain.subscription.rest.dto;

import club.ttg.dnd5.domain.achievement.rest.dto.UserAchievementResponse;
import club.ttg.dnd5.domain.subscription.model.RewardTier;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Погашенный текущим пользователем код вместе с его содержимым для личного кабинета.
 * В отличие от админского {@link RedemptionCodeResponse}, не раскрывает сам код и
 * служебные поля, зато сразу резолвит награды в ссылки/статусы готовности.
 *
 * @param id           идентификатор кода
 * @param code         сам погашенный код (виден владельцу)
 * @param redeemedAt   момент погашения
 * @param rewardTier   тир-пресет кода (null — без пресета)
 * @param rewards      перки кода (пресет тира ∪ доп. перки) со ссылками и статусом
 * @param achievements достижения кода с названиями из каталога
 * @param subscription подписка, созданная этим кодом; null — код без подписки
 */
public record MyRedemptionResponse(
        UUID id,
        String code,
        Instant redeemedAt,
        RewardTier rewardTier,
        List<UserRewardResponse> rewards,
        List<UserAchievementResponse> achievements,
        SubscriptionResponse subscription
) {
}
