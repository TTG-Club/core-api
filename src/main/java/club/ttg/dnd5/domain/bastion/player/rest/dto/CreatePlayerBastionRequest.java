package club.ttg.dnd5.domain.bastion.player.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Новый бастион в игре. Создаёт только мастер игры.
 *
 * @param gameId  игра каталога
 * @param name    название бастиона
 * @param members игроки с доступом; можно пустым и добавить позже
 */
@Schema(description = "Создание бастиона")
public record CreatePlayerBastionRequest(
        @NotNull UUID gameId,
        @NotBlank @Size(max = 100) String name,
        List<@Valid PlayerBastionMemberRequest> members) {
}
