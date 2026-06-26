package club.ttg.dnd5.domain.subscription.rest.dto;

import club.ttg.dnd5.domain.subscription.model.RewardResourceAvailability;
import jakarta.validation.constraints.NotNull;

/**
 * Обновление контента награды (например, включить приключение, когда готово).
 */
public record UpdateRewardResourceRequest(
        String title,
        String url,
        @NotNull RewardResourceAvailability availability,
        String note
) {
}
