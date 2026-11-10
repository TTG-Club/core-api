package club.ttg.dnd5.domain.bastion.player.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Новый бастион. Создаёт любой пользователь: бастион принадлежит ему, а его персонаж сразу
 * получает доступ.
 *
 * @param name             название бастиона
 * @param characterName    имя персонажа владельца; пусто — «Персонаж»
 * @param characterLevel   уровень персонажа: от него зависит число специализированных сооружений
 * @param characterSheetId лист персонажа, если владелец хочет его привязать
 * @param basic            стартовые базовые сооружения персонажа: тесное и вместительное.
 *                         Заданы — бастион сразу действует; пусто — создаётся в закладке
 * @param special          стартовые специализированные сооружения — не больше лимита по уровню
 */
@Schema(description = "Создание бастиона")
public record CreatePlayerBastionRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String characterName,
        @NotNull @Min(1) @Max(20) Integer characterLevel,
        UUID characterSheetId,
        List<FacilitySetupRequest.@Valid Basic> basic,
        List<FacilitySetupRequest.@Valid Special> special) {

    public CreatePlayerBastionRequest(String name, String characterName, Integer characterLevel,
                                      UUID characterSheetId) {
        this(name, characterName, characterLevel, characterSheetId, null, null);
    }
}
