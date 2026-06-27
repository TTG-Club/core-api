package club.ttg.dnd5.domain.achievement.rest.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;
import java.util.UUID;

/**
 * Межсервисная выдача достижений из subscriber-service: погашение кода/награды
 * на стороне subscriber-service приводит к выдаче достижений, которые остаются
 * в core-api.
 *
 * @param username     кому выдать
 * @param achievements коды достижений из каталога
 * @param sourceCode   код-источник (промо-код в subscriber-service), может быть null
 */
public record GrantAchievementsInternalRequest(
        @NotBlank String username,
        @NotEmpty Set<String> achievements,
        UUID sourceCode
) {
}
