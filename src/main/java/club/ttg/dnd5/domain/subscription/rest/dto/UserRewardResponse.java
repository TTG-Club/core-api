package club.ttg.dnd5.domain.subscription.rest.dto;

import club.ttg.dnd5.domain.subscription.model.RewardPerk;
import club.ttg.dnd5.domain.subscription.model.RewardResourceAvailability;

import java.time.Instant;

/**
 * Награда пользователя вместе с её контентом (ссылка/статус готовности).
 */
public record UserRewardResponse(
        RewardPerk perk,
        Instant grantedAt,
        String title,
        String url,
        RewardResourceAvailability availability,
        String note
) {
}
