package club.ttg.dnd5.domain.bastion.player.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Добавление своего бастиона в игру.
 *
 * @param gameId игра каталога, участник которой — владелец бастиона
 */
@Schema(description = "Добавление бастиона в игру")
public record JoinGameRequest(@NotNull UUID gameId) {
}
