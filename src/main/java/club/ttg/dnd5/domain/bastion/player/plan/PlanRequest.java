package club.ttg.dnd5.domain.bastion.player.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Сохранение плана.
 *
 * @param version  версия плана, с которой начата правка; 0 — плана ещё не было
 * @param document план целиком
 */
@Schema(description = "Сохранение плана бастиона")
public record PlanRequest(@NotNull Long version, @NotNull PlanDocument document) {
}
