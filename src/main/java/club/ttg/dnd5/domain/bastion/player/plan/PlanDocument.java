package club.ttg.dnd5.domain.bastion.player.plan;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * План бастиона — один документ на бастион, упрощённая карта в духе карт подземелий.
 * Координаты — в клетках 5×5 футов.
 *
 * <p>У каждого объекта плана свой идентификатор (у дверей и окон — ребро клетки), а
 * пересечения клеток сервер разрешает сам: так правки нескольких человек сливаются
 * без отказа в сохранении.</p>
 *
 * <p>План собирается из комнат на этажах. Комната — набор клеток одного этажа: зал, коридор,
 * открытый двор или круглая башня. Стены не хранятся: редактор строит их сам по границам
 * комнат. Двери и окна стоят на рёбрах клеток, лестницы и колонны — на полу.</p>
 *
 * <p>Площадь сооружения по правилам — число клеток всех комнат, которым оно назначено.
 * План косметический: перерисовка ничего не стоит. По правилам проверяются только площадь
 * и целостность документа ({@code PlanRules}).</p>
 *
 * @param levels        этажи плана; 0 — первый этаж, отрицательные — подвалы
 * @param rooms         комнаты всех этажей
 * @param doors         двери в стенах между клетками
 * @param windows       окна в стенах между клетками
 * @param towerOpenings двери и окна в круглой стене башен
 * @param stairs        лестницы
 * @param columns       колонны
 * @param defenseWalls  клетки защитной стены (на уровне земли)
 * @param furniture     мебель
 * @param defenseGates  клетки защитной стены, где стоят ворота
 */
@Schema(description = "План бастиона")
public record PlanDocument(List<Integer> levels,
                           List<Room> rooms,
                           List<Door> doors,
                           List<Window> windows,
                           List<TowerOpening> towerOpenings,
                           List<Stair> stairs,
                           List<Column> columns,
                           List<Cell> defenseWalls,
                           List<Furniture> furniture,
                           List<Cell> defenseGates) {

    public static PlanDocument empty() {
        return new PlanDocument(List.of(0), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of());
    }

    /**
     * Вид комнаты. Коридоры, соединённые друг с другом, стены между собой не получают; у двора
     * вместо стены — пунктир; у башни круглая стена, клетки считаются по её окружности.
     */
    public enum RoomKind { ROOM, CORRIDOR, COURTYARD, TOWER }

    /** Сторона клетки, на ребре которой стоит дверь или окно. */
    public enum Side { N, E, S, W }

    /** Дверь: обычная, запертая, секретная, решётка (глава 3 «Двери») или открытый проём. */
    public enum DoorKind { DOOR, LOCKED, SECRET, PORTCULLIS, ARCH }

    /** Что стоит в стене башни. */
    public enum OpeningType { DOOR, WINDOW }

    /** Лестница: прямой марш или винтовая. */
    public enum StairKind { STRAIGHT, SPIRAL }

    /**
     * Предмет интерьера и его размер в клетках без поворота: ширина вдоль x, глубина вдоль y.
     * Предметы с признаком {@code premium} ставятся только в бастион владельца с подпиской.
     */
    public enum FurnitureKind {
        TABLE(2, 1, false),
        CHAIR(1, 1, false),
        WARDROBE(2, 1, false),
        SOFA(2, 1, false),
        BED(1, 2, false),
        CHEST(1, 1, false),
        STATUE(1, 1, true),
        FOUNTAIN(2, 2, true),
        ALTAR(2, 1, true),
        THRONE(1, 1, true),
        BOOKSHELF(2, 1, true),
        FIREPLACE(2, 1, true),
        DINING_TABLE(4, 2, true),
        BARREL(1, 1, true),
        PANTRY_RACK(2, 1, true),
        WORKBENCH(2, 1, true),
        ANVIL(1, 1, true),
        ALCHEMY_TABLE(2, 1, true),
        WEAPON_RACK(2, 1, true),
        TRAINING_DUMMY(1, 1, true),
        CAGE(2, 2, true),
        RUG(3, 2, true),
        TAPESTRY(2, 1, true),
        WELL(1, 1, true),
        TREE(2, 2, true),
        FLOWERBED(2, 1, true),
        TELESCOPE(1, 1, true),
        MAGIC_CIRCLE(3, 3, true);

        private final int width;
        private final int depth;
        private final boolean premium;

        FurnitureKind(int width, int depth, boolean premium) {
            this.width = width;
            this.depth = depth;
            this.premium = premium;
        }

        public int width() {
            return width;
        }

        public int depth() {
            return depth;
        }

        /** Доступен только бастиону владельца с подпиской. */
        public boolean premium() {
            return premium;
        }
    }

    /**
     * Комната.
     *
     * @param id         идентификатор внутри плана (генерирует редактор)
     * @param name       название: «Главный зал»
     * @param kind       вид комнаты
     * @param level      этаж
     * @param facilityId сооружение персонажа, которое здесь стоит; {@code null} — просто помещение
     * @param cells      клетки; у башни сервер пересчитывает их по окружности
     * @param shape      окружность башни; у остальных комнат {@code null}
     */
    @Schema(description = "Комната")
    public record Room(String id, String name, RoomKind kind, int level, UUID facilityId, List<Cell> cells,
                       Circle shape) {
    }

    /**
     * Окружность башни в клетках: центр и радиус, кратные половине клетки. Клетки башни —
     * те, чей центр лежит внутри окружности.
     */
    @Schema(description = "Окружность башни")
    public record Circle(double cx, double cy, double r) {
    }

    /**
     * Дверь на ребре клетки.
     *
     * @param side сторона клетки {@code (x, y)}; ребро с соседней клеткой одно и то же
     */
    @Schema(description = "Дверь")
    public record Door(int level, int x, int y, Side side, DoorKind kind) {
    }

    @Schema(description = "Окно")
    public record Window(int level, int x, int y, Side side) {
    }

    /**
     * Дверь или окно в стене башни.
     *
     * @param angle угол в градусах от направления на восток по часовой стрелке, шаг 22.5°
     * @param kind  вид двери; у окна {@code null}
     */
    @Schema(description = "Проём в стене башни")
    public record TowerOpening(String roomId, double angle, OpeningType type, DoorKind kind) {
    }

    /**
     * Лестница — прямоугольник клеток.
     *
     * @param id        идентификатор внутри плана (генерирует редактор)
     * @param direction куда идёт подъём
     * @param toLevel   этаж, на который ведёт лестница
     */
    @Schema(description = "Лестница")
    public record Stair(String id, int level, int x, int y, int width, int height, Side direction, int toLevel,
                        StairKind kind) {
    }

    /**
     * Колонна в центре клетки или на пересечении линий сетки: координаты кратны половине клетки.
     *
     * @param id идентификатор внутри плана (генерирует редактор)
     */
    @Schema(description = "Колонна")
    public record Column(String id, int level, double x, double y) {
    }

    /**
     * Мебель: прямоугольник клеток от угла {@code (x, y)}; при повороте на 90° и 270° ширина и
     * глубина меняются местами.
     *
     * @param id       идентификатор внутри плана (генерирует редактор)
     * @param rotation поворот по часовой стрелке: 0, 90, 180 или 270 градусов
     */
    @Schema(description = "Мебель")
    public record Furniture(String id, int level, int x, int y, FurnitureKind kind, int rotation) {
    }

    @Schema(description = "Клетка")
    public record Cell(int x, int y) {
    }
}
