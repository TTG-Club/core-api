package club.ttg.dnd5.domain.bastion.player.plan;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * План бастиона глазами открывшего.
 *
 * @param version  версия плана; 0 — план ещё не сохраняли
 * @param canEdit  открывший может рисовать: мастер или игрок с доступом, бастион не в архиве
 * @param premium  у владельца бастиона подписка: можно ставить предметы интерьера по подписке
 */
@Schema(description = "План бастиона")
public record PlanResponse(UUID bastionId, long version, boolean canEdit, boolean premium,
                           PlanDocument document) {
}
