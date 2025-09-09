package club.ttg.dnd5.domain.character_class.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassTableItem {
    @Schema(description = "Уровень класса")
    private int level;
    @Schema(description = "Значение строки")
    private String value;
}
