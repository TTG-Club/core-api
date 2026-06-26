package club.ttg.dnd5.domain.achievement.rest.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/**
 * Прямая выдача достижений пользователю админом (минуя коды).
 */
public record GrantAchievementsRequest(
        @NotEmpty Set<String> achievementCodes
) {
}
