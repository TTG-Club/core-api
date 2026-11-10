package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Запросы и редакторы одного листа — для окна «Поделиться» у владельца.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetEditorListResponse {

    @Schema(description = "Максимум редакторов у листа")
    private int limit;

    @Schema(description = "Неотвеченные запросы и выданные права, старые первее")
    private List<CharacterSheetEditorResponse> editors;
}
