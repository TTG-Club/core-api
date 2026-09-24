package club.ttg.dnd5.domain.bastion.player.plan;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * План бастиона — один документ на бастион. Координаты — в клетках 5×5 футов.
 *
 * <p>Бастион собирается из корпусов (башня, донжон, крыло, флигель). У корпуса векторный
 * контур и свои этажи; сооружения закрашивают клетки этажа внутри контура. Площадь по
 * правилам — число клеток сооружения по всем этажам и корпусам. Переходы и защитная стена
 * лежат вне корпусов.</p>
 *
 * <p>План косметический: перерисовка ничего не стоит. По правилам проверяются только
 * площадь и геометрия ({@code PlanRules}).</p>
 *
 * @param buildings корпуса
 * @param passages  коридоры, галереи и мосты вне корпусов
 * @param walls     клетки защитной стены (на уровне земли)
 */
@Schema(description = "План бастиона")
public record PlanDocument(List<Building> buildings, List<Passage> passages, List<Cell> walls) {

    public static PlanDocument empty() {
        return new PlanDocument(List.of(), List.of(), List.of());
    }

    /** Вид корпуса — подпись и подсказка редактору, на правила не влияет. */
    public enum BuildingKind { TOWER, KEEP, WING, HALL, OUTBUILDING }

    /** Вид контура. */
    public enum OutlineType { CIRCLE, RECTANGLE, POLYGON }

    /** Сторона клетки, на которой стоит дверь или окно. */
    public enum Side { N, E, S, W }

    /** Дверь: обычная, запертая, секретная, решётка (глава 3 «Двери»). */
    public enum DoorKind { DOOR, LOCKED, SECRET, PORTCULLIS }

    /** Переход: коридор, пандус, галерея или мост. В площадь сооружений не входит. */
    public enum PassageKind { CORRIDOR, RAMP, GALLERY, BRIDGE }

    /**
     * Корпус.
     *
     * @param id      идентификатор внутри плана (генерирует редактор)
     * @param name    название: «Северная башня»
     * @param kind    вид корпуса
     * @param outline контур на земле
     * @param floors  этажи; уровень 0 — первый этаж, отрицательные — подвалы
     */
    @Schema(description = "Корпус")
    public record Building(String id, String name, BuildingKind kind, Outline outline, List<Floor> floors) {
    }

    /**
     * Контур корпуса в клетках. Окружность — центр и радиус, прямоугольник — угол и
     * размеры, многоугольник — вершины. Значения могут быть дробными (кратными 0.5),
     * чтобы центр окружности вставал и в угол клетки, и в её середину.
     */
    @Schema(description = "Контур корпуса")
    public record Outline(OutlineType type,
                          Double cx, Double cy, Double r,
                          Double x, Double y, Double width, Double height,
                          List<List<Double>> points) {
    }

    /**
     * Этаж корпуса.
     *
     * @param level   уровень этажа
     * @param cells   клетки сооружений
     * @param doors   двери
     * @param windows окна
     * @param stairs  лестницы на другие этажи этого корпуса
     */
    @Schema(description = "Этаж корпуса")
    public record Floor(int level, List<FacilityCell> cells, List<Door> doors, List<Window> windows,
                        List<Stair> stairs) {
    }

    /** Клетка, занятая сооружением персонажа. */
    @Schema(description = "Клетка сооружения")
    public record FacilityCell(int x, int y, UUID facilityId) {
    }

    @Schema(description = "Дверь")
    public record Door(int x, int y, Side side, DoorKind kind) {
    }

    @Schema(description = "Окно")
    public record Window(int x, int y, Side side) {
    }

    @Schema(description = "Лестница")
    public record Stair(int x, int y, int toLevel) {
    }

    /**
     * Переход вне корпусов.
     *
     * @param level уровень: галерея и мост могут идти на втором этаже
     */
    @Schema(description = "Переход")
    public record Passage(PassageKind kind, int level, List<Cell> cells) {
    }

    @Schema(description = "Клетка")
    public record Cell(int x, int y) {
    }
}
