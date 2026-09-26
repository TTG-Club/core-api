package club.ttg.dnd5.domain.bastion.player.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Мастер подтверждает или снимает подтверждение требования сооружения
 * (фокусировка, компетентность и т.п.).
 */
@Schema(description = "Подтверждение требования сооружения")
public record PrerequisiteConfirmationRequest(@NotNull Boolean confirmed) {
}
