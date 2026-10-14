package club.ttg.dnd5.domain.bastion.player.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Бастионы пользователя и его лимит.
 *
 * @param limit           сколько бастионов можно создать сейчас
 * @param subscriberLimit сколько даёт подписка: подсказка «с подпиской доступно больше»
 * @param bastions        свои бастионы, старые первыми
 */
@Schema(description = "Мои бастионы")
public record MyPlayerBastionsResponse(int limit, int subscriberLimit, List<PlayerBastionResponse> bastions) {
}
