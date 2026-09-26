package club.ttg.dnd5.domain.bastion.player.rest.dto;

import club.ttg.dnd5.domain.bastion.player.client.GameRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * Бастионы игры одним ответом — для страницы «Бастионы» игры.
 *
 * @param role      кем открывший приходится игре
 * @param canCreate может создавать бастионы (мастер)
 * @param players   игроки с одобренной заявкой — кому мастер может дать доступ; другим пусто
 * @param bastions  все бастионы игры: смотреть их может любой участник
 */
@Schema(description = "Бастионы игры")
public record PlayerBastionGameResponse(
        UUID gameId,
        String title,
        GameRole role,
        boolean canCreate,
        List<Player> players,
        List<PlayerBastionResponse> bastions) {

    /**
     * Игрок игры.
     *
     * @param characterName имя персонажа из заявки; может быть пустым
     */
    @Schema(description = "Игрок игры")
    public record Player(UUID userId, String characterName) {
    }
}
