package club.ttg.dnd5.domain.subscription.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record RedeemCodeRequest(
        @NotBlank String code
) {
}
