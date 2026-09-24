package club.ttg.dnd5.domain.bastion.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Выбор, который игрок делает для своего экземпляра сооружения: тип сада, мастер
 * тренировочной зоны, тип гильдии, справочник архива, напиток трактира, инструменты
 * мастерской.
 *
 * <p>В справочнике хранится только список вариантов и сколько их берётся; сделанный выбор
 * будет жить в бастионе игрока.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Выбор для сооружения")
public class FacilityChoice {
    @Schema(description = "Название выбора", examples = {"Тип сада", "Мастер"})
    private String name;
    @Schema(description = "Пояснение: когда выбирается и можно ли сменить")
    private String description;
    @Schema(description = "Сколько вариантов выбирается")
    private Integer count;
    @Schema(description = "Сколько вариантов выбирается у расширенного сооружения")
    private Integer enlargedCount;
    @Schema(description = "Варианты")
    private List<FacilityChoiceOption> options;
}
