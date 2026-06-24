package club.ttg.dnd5.domain.subscription.rest.dto;

import club.ttg.dnd5.domain.subscription.model.RewardPerk;
import club.ttg.dnd5.domain.subscription.model.RewardTier;
import club.ttg.dnd5.domain.subscription.model.SubscriptionType;
import jakarta.validation.constraints.Min;

import java.util.Set;

/**
 * Запрос на выпуск пачки кодов с одинаковым содержимым.
 * Код должен нести хотя бы что-то одно: подписку, тир, перки или достижения.
 *
 * @param subscriptionType   тип подписки (нужен, если задан subscriptionMonths)
 * @param subscriptionMonths срок подписки в месяцах; null — код без подписки
 * @param rewardTier         краудфандинговый тир-пресет; null — без пресета
 * @param perks              произвольный набор косметических перков помимо тира
 * @param achievements       произвольный набор кодов достижений
 * @param count              сколько одинаковых кодов выпустить (по умолчанию 1)
 * @param label              пометка админа для всей пачки
 */
public record CreateCodesRequest(
        SubscriptionType subscriptionType,
        @Min(1) Integer subscriptionMonths,
        RewardTier rewardTier,
        Set<RewardPerk> perks,
        Set<String> achievements,
        @Min(1) Integer count,
        String label
) {
}
