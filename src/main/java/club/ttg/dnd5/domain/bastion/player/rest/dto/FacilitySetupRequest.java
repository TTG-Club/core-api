package club.ttg.dnd5.domain.bastion.player.rest.dto;

import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.model.SelectedChoice;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Стартовый выбор сооружений персонажа целиком: список заменяет прежний. Можно
 * сохранять частично — сервер проверяет только, что правила не нарушены.
 *
 * @param basic   базовые сооружения: одно тесное и одно вместительное
 * @param special специализированные — не больше лимита по уровню
 * @param version версия бастиона, с которой начата правка; устарела — 409
 */
@Schema(description = "Стартовый выбор сооружений персонажа")
public record FacilitySetupRequest(
        @NotNull List<@Valid Basic> basic,
        @NotNull List<@Valid Special> special,
        @NotNull Long version) {

    /**
     * Базовое сооружение и его пространство.
     *
     * @param facilityUrl сооружение справочника
     * @param space       тесное или вместительное
     */
    @Schema(description = "Базовое сооружение")
    public record Basic(@NotBlank String facilityUrl, @NotNull FacilitySpace space) {
    }

    /**
     * Специализированное сооружение и сделанные для него выборы.
     *
     * @param facilityUrl сооружение справочника
     * @param choices     тип сада, мастер и т.п.
     */
    @Schema(description = "Специализированное сооружение")
    public record Special(@NotBlank String facilityUrl, List<SelectedChoice> choices) {
    }
}
