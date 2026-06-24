package club.ttg.dnd5.domain.achievement.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Создание/обновление достижения в каталоге (админ). Код берётся из пути.
 */
public record AchievementRequest(
        @NotBlank String title,
        String description,
        String icon,
        boolean hidden,
        String triggerKey
) {
}
