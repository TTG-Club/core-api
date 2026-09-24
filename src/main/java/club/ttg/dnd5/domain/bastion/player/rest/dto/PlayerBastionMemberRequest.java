package club.ttg.dnd5.domain.bastion.player.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Игрок, которому мастер даёт доступ к бастиону, и его персонаж.
 *
 * @param userId           игрок игры с одобренной заявкой
 * @param characterName    имя персонажа; пусто — берётся из заявки в игру
 * @param characterLevel   уровень персонажа: от него зависит число специализированных сооружений
 * @param characterSheetId лист персонажа, если мастер хочет его привязать
 */
@Schema(description = "Игрок с доступом к бастиону")
public record PlayerBastionMemberRequest(
        @NotNull UUID userId,
        @Size(max = 100) String characterName,
        @NotNull @Min(1) @Max(20) Integer characterLevel,
        UUID characterSheetId) {
}
