package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Пользователь, у которого лист сейчас открыт. Только имя и аватарка — как в списке редакторов.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetPresenceUserResponse {

    @NotNull
    @Schema(description = "Отображаемое имя")
    private String displayName;

    @Nullable
    @Schema(description = "Ссылка на аватарку; null — аватарки нет")
    private String avatarUrl;
}
