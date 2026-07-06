package club.ttg.dnd5.domain.subscription.rest.dto;

import club.ttg.dnd5.domain.subscription.model.RewardPerk;
import club.ttg.dnd5.domain.subscription.model.RewardResourceAvailability;

import java.time.Instant;

public record RewardResourceResponse(
        RewardPerk perk,
        String title,
        String url,
        RewardResourceAvailability availability,
        String note,
        Instant updatedAt
) {
}
