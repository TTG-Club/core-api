package club.ttg.dnd5.domain.subscription.rest.dto;

import jakarta.validation.constraints.Min;

public record CreateGiftSubscriptionRequest(
        @Min(1) int durationMonths
) {
}
