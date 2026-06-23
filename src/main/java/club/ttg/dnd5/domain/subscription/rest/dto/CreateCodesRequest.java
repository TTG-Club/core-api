package club.ttg.dnd5.domain.subscription.rest.dto;

import club.ttg.dnd5.domain.subscription.model.RewardTier;
import club.ttg.dnd5.domain.subscription.model.SubscriptionType;
import jakarta.validation.constraints.Min;

/**
 * Запрос на выпуск пачки кодов с одинаковыми наградами и периодом.
 * Хотя бы одно из (subscriptionMonths, rewardTier) должно быть задано.
 *
 * @param subscriptionType   тип подписки (нужен, если задан subscriptionMonths)
 * @param subscriptionMonths срок подписки в месяцах; null — код без подписки
 * @param rewardTier         краудфандинговый тир наград; null — код без наград
 * @param count              сколько одинаковых кодов выпустить (по умолчанию 1)
 * @param label              пометка админа для всей пачки
 */
public record CreateCodesRequest(
        SubscriptionType subscriptionType,
        @Min(1) Integer subscriptionMonths,
        RewardTier rewardTier,
        @Min(1) Integer count,
        String label
) {
}
