package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Кто ещё держит лист открытым — ответ на отметку присутствия.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetPresenceResponse {

    @Schema(description = "Другие пользователи, у которых лист открыт прямо сейчас; сам спросивший не входит")
    private List<CharacterSheetPresenceUserResponse> users;
}
