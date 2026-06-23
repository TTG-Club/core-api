package club.ttg.dnd5.domain.subscription.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterSubscriptionRequest(
        @NotBlank String code
) {
}
