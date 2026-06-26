package club.ttg.dnd5.domain.achievement.rest.dto;

import java.time.Instant;

/**
 * Достижение, полученное пользователем, вместе с его описанием из каталога.
 */
public record UserAchievementResponse(
        String code,
        String title,
        String description,
        String icon,
        Instant grantedAt
) {
}
