package club.ttg.dnd5.domain.bastion.model;

import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.FurnitureKind;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Шаблон готовой комнаты сооружения для плана бастиона: прямоугольник площадью пространства и
 * мебель в нём. Когда сооружение перетаскивают на план, комната встаёт по шаблону его
 * пространства; без шаблона редактор плана расставляет мебель сам.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Шаблон комнаты сооружения на плане")
public class FacilityRoomTemplate {
    @Schema(description = "Пространство, для которого шаблон")
    private FacilitySpace space;
    @Schema(description = "Ширина комнаты, клетки")
    private int width;
    @Schema(description = "Глубина комнаты, клетки")
    private int depth;
    @Schema(description = "Мебель: координаты левой верхней клетки от угла комнаты")
    private List<Item> furniture;

    /** Предмет мебели в шаблоне. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Предмет мебели в шаблоне комнаты")
    public static class Item {
        private FurnitureKind kind;
        private int x;
        private int y;
        @Schema(description = "Поворот по часовой: 0, 90, 180 или 270")
        private int rotation;
    }
}
