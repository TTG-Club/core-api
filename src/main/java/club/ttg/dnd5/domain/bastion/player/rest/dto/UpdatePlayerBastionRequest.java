package club.ttg.dnd5.domain.bastion.player.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Правка бастиона мастером: название и состав игроков целиком.
 *
 * @param name    название бастиона
 * @param members игроки с доступом — список заменяет прежний
 * @param version версия, с которой мастер начал правку; устарела — 409
 */
@Schema(description = "Правка бастиона")
public record UpdatePlayerBastionRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull List<@Valid PlayerBastionMemberRequest> members,
        @NotNull Long version) {
}
