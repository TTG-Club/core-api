package club.ttg.dnd5.domain.bastion.player.share;

import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionResponse;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Бастион, открытый по ссылке «поделиться»: только чтение и то, что открывший может сделать
 * дальше.
 *
 * @param bastion бастион без прав на правку
 * @param access  доступ открывшего
 */
@Schema(description = "Бастион по ссылке")
public record SharedPlayerBastionResponse(PlayerBastionResponse bastion, Access access) {

    /** Доступ открывшего к бастиону. */
    public enum Access {
        /** Аноним: чтобы попросить право на правку, нужно войти. */
        ANONYMOUS,
        /** Можно попросить право на правку. */
        NONE,
        /** Запрос ждёт ответа владельца. */
        PENDING,
        /** Бастион открывается полностью: владелец, редактор, мастер или игрок с доступом. */
        GRANTED
    }
}
