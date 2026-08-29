package club.ttg.dnd5.domain.user.rest.dto;

import java.util.UUID;

/**
 * Пара «идентификатор пользователя → отображаемое имя».
 *
 * Нужна сервисам, которые хранят только {@code sub} токена и не знают логинов:
 * например, find-game-api подписывает участников чата игры. Без такого резолва
 * на экране остался бы сырой UUID.
 */
public record DisplayNameByUserIdResponse(UUID userId, String displayName) {
}
