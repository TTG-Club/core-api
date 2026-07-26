package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Ответ на включение доступа по ссылке. Ссылку собирает клиент — сервер не знает
 * ни домена фронтенда, ни его маршрутов.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetShareResponse {

    @NotNull
    @Schema(description = "Токен ссылки: по нему лист открывается на чтение без авторизации")
    private UUID shareToken;
}
