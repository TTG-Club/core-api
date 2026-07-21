package club.ttg.dnd5.domain.spellbook.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SpellbookLevelGroupResponse {

    @Schema(description = "Уровень заклинаний группы: 0 — заговоры")
    private long level;

    @NotNull
    @Schema(description = "Человеко-читаемое название группы: «Заговоры» или «1 уровень»")
    private String levelName;

    @Schema(description = "Всего заклинаний в группе")
    private long spellCount;

    @Schema(description = "Из них подготовленных")
    private long preparedCount;

    @NotNull
    @Schema(description = "Заклинания группы по алфавиту")
    private List<SpellbookSpellResponse> spells;
}
