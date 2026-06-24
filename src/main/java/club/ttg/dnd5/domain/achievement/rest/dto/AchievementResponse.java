package club.ttg.dnd5.domain.achievement.rest.dto;

/**
 * Достижение из каталога (для админки и общего списка).
 */
public record AchievementResponse(
        String code,
        String title,
        String description,
        String icon,
        boolean hidden,
        String triggerKey
) {
}
