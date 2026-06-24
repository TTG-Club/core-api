package club.ttg.dnd5.domain.subscription.rest.dto;

import club.ttg.dnd5.domain.subscription.model.RewardPerk;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/**
 * Прямая выдача косметических перков пользователю админом (минуя коды).
 */
public record GrantPerksRequest(
        @NotEmpty Set<RewardPerk> perks
) {
}
