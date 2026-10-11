package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Кто ещё держит лист открытым и какая версия листа сейчас на сервере — ответ на отметку присутствия.
 * По версии клиент узнаёт, что лист сохранил другой редактор, и подтягивает его правки, не дожидаясь
 * своего сохранения.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetPresenceResponse {

    @Schema(description = "Другие пользователи, у которых лист открыт прямо сейчас; сам спросивший не входит")
    private List<CharacterSheetPresenceUserResponse> users;

    @Schema(description = "Текущая версия листа на сервере")
    private long version;
}
