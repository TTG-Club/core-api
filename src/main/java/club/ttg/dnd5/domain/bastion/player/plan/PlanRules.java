package club.ttg.dnd5.domain.bastion.player.plan;

import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Cell;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Circle;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Column;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Door;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.OpeningType;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Room;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.RoomKind;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Side;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Stair;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.TowerOpening;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Window;
import club.ttg.dnd5.exception.ApiException;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Проверка плана бастиона.
 *
 * <p>План косметический, поэтому строго проверяется только то, что относится к правилам и
 * к целостности документа:
 * <ul>
 *     <li>площадь каждого сооружения — сумма клеток его комнат по всем этажам — не больше
 *     его пространства (тесное 4, вместительное 16, просторное 36);</li>
 *     <li>на плане только сооружения этого бастиона;</li>
 *     <li>клетка этажа принадлежит одной комнате — пересечения разводит {@link #normalize};</li>
 *     <li>двери и окна стоят на краю комнаты, лестница ведёт на существующий этаж;</li>
 *     <li>размеры плана ограничены, чтобы документ не разрастался бесконечно.</li>
 * </ul>
 * Где именно встают стены, сервер не проверяет: их строит редактор.</p>
 */
@UtilityClass
public class PlanRules {
    /** Сторона участка в клетках: 200 клеток — 1000 футов. */
    public static final int GRID_SIZE = 200;
    public static final int MAX_FLOORS = 10;
    public static final int MIN_LEVEL = -3;
    public static final int MAX_LEVEL = 9;
    public static final int MAX_ROOMS = 300;
    public static final int MAX_CELLS = 20_000;
    public static final int MAX_OBJECTS = 2_000;
    public static final int MAX_WALL_CELLS = 4_000;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_STAIR_SIDE = 10;
    public static final double MIN_TOWER_RADIUS = 1;
    public static final double MAX_TOWER_RADIUS = 25;
    /** Шаг угла проёма в стене башни, градусы. */
    public static final double TOWER_ANGLE_STEP = 22.5;

    /**
     * Приводит план к виду, который сервер хранит: клетки башен пересчитаны по окружности,
     * сооружения, убранные из бастиона, сняты с комнат, пустые списки вместо {@code null}.
     *
     * @param document   план от клиента или из базы
     * @param facilities сооружения бастиона
     * @return нормализованный план
     */
    public static PlanDocument normalize(PlanDocument document, Map<UUID, ?> facilities) {
        if (document == null) {
            return PlanDocument.empty();
        }
        List<Integer> levels = list(document.levels());
        List<Room> rooms = list(document.rooms()).stream()
                .map(room -> new Room(room.id(), room.name(), room.kind(), room.level(),
                        room.facilityId() != null && facilities.containsKey(room.facilityId())
                                ? room.facilityId() : null,
                        room.kind() == RoomKind.TOWER && validCircle(room.shape())
                                ? PlanGeometry.cellsInside(room.shape()) : list(room.cells()),
                        room.kind() == RoomKind.TOWER ? room.shape() : null))
                .toList();
        return new PlanDocument(levels.isEmpty() ? List.of(0) : levels, withoutOverlaps(rooms),
                list(document.doors()), list(document.windows()), list(document.towerOpenings()),
                list(document.stairs()), list(document.columns()), list(document.defenseWalls()));
    }

    /**
     * Разводит комнаты, претендующие на одну клетку этажа: клетка остаётся за башней, а среди
     * прочих — за комнатой, что раньше в списке. Так одновременные правки двух человек дают
     * предсказуемый план, а не отказ в сохранении. Опустевшие комнаты убираются.
     */
    private static List<Room> withoutOverlaps(List<Room> rooms) {
        Map<Integer, Set<Cell>> taken = new HashMap<>();
        List<Room> ordered = new ArrayList<>(rooms.stream().filter(room -> room.kind() == RoomKind.TOWER).toList());
        ordered.addAll(rooms.stream().filter(room -> room.kind() != RoomKind.TOWER).toList());

        Map<Room, List<Cell>> kept = new HashMap<>();
        for (Room room : ordered) {
            Set<Cell> floor = taken.computeIfAbsent(room.level(), level -> new HashSet<>());
            kept.put(room, room.cells().stream().filter(floor::add).toList());
        }
        return rooms.stream()
                .map(room -> new Room(room.id(), room.name(), room.kind(), room.level(), room.facilityId(),
                        kept.get(room), room.shape()))
                .filter(room -> !room.cells().isEmpty())
                .toList();
    }

    /**
     * Проверяет нормализованный план.
     *
     * @param document   план после {@link #normalize}
     * @param facilities сооружения бастиона: id → пространство
     * @param names      названия сооружений для сообщений об ошибке
     * @throws ApiException 400 с объяснением
     */
    public static void validate(PlanDocument document,
                                Map<UUID, FacilitySpace> facilities,
                                Map<UUID, String> names) {
        Set<Integer> levels = validateLevels(document.levels());
        Map<Integer, Map<Cell, Room>> floors = validateRooms(document.rooms(), levels);
        validateArea(document.rooms(), facilities, names);

        int objects = document.doors().size() + document.windows().size() + document.towerOpenings().size()
                + document.stairs().size() + document.columns().size();
        if (objects > MAX_OBJECTS) {
            throw badRequest("Слишком много дверей, окон, лестниц и колонн: больше %d".formatted(MAX_OBJECTS));
        }

        Set<String> edges = new HashSet<>();
        for (Door door : document.doors()) {
            if (door.kind() == null) {
                throw badRequest("У двери не указан вид");
            }
            requireEdge(floors, edges, door.level(), door.x(), door.y(), door.side(), "Дверь");
        }
        for (Window window : document.windows()) {
            requireEdge(floors, edges, window.level(), window.x(), window.y(), window.side(), "Окно");
        }
        validateTowerOpenings(document.towerOpenings(), document.rooms());
        Set<String> objectIds = new HashSet<>();
        for (Stair stair : document.stairs()) {
            requireObjectId(objectIds, stair.id());
            validateStair(stair, levels);
        }
        for (Column column : document.columns()) {
            requireObjectId(objectIds, column.id());
            requireLevel(levels, column.level());
            if (!halfStep(column.x()) || !halfStep(column.y()) || !onGrid(column.x(), column.y())) {
                throw badRequest("Колонна стоит вне сетки участка");
            }
        }

        if (document.defenseWalls().size() > MAX_WALL_CELLS) {
            throw badRequest("Защитная стена длиннее %d клеток".formatted(MAX_WALL_CELLS));
        }
        document.defenseWalls().forEach(PlanRules::requireCellOnGrid);
    }

    private static Set<Integer> validateLevels(List<Integer> levels) {
        Set<Integer> unique = new HashSet<>(levels);
        if (unique.size() != levels.size() || levels.size() > MAX_FLOORS) {
            throw badRequest("Этажей должно быть от 1 до %d, без повторов".formatted(MAX_FLOORS));
        }
        if (!unique.contains(0)) {
            throw badRequest("У плана должен быть первый этаж");
        }
        for (int level : unique) {
            if (level < MIN_LEVEL || level > MAX_LEVEL) {
                throw badRequest("Этаж должен быть от %d до %d".formatted(MIN_LEVEL, MAX_LEVEL));
            }
        }
        return unique;
    }

    /** Комнаты и клетки этажей: уровень → клетка → комната. */
    private static Map<Integer, Map<Cell, Room>> validateRooms(List<Room> rooms, Set<Integer> levels) {
        if (rooms.size() > MAX_ROOMS) {
            throw badRequest("На плане не больше %d комнат".formatted(MAX_ROOMS));
        }
        int total = rooms.stream().mapToInt(room -> room.cells().size()).sum();
        if (total > MAX_CELLS) {
            throw badRequest("Слишком большой план: больше %d клеток".formatted(MAX_CELLS));
        }

        Set<String> ids = new HashSet<>();
        Map<Integer, Map<Cell, Room>> floors = new HashMap<>();
        for (Room room : rooms) {
            if (!StringUtils.hasText(room.id()) || !ids.add(room.id())) {
                throw badRequest("У каждой комнаты должен быть свой идентификатор");
            }
            String title = title(room);
            if (room.name() != null && room.name().length() > MAX_NAME_LENGTH) {
                throw badRequest("Название «%s» длиннее %d символов".formatted(title, MAX_NAME_LENGTH));
            }
            if (room.kind() == null) {
                throw badRequest("У «%s» не указан вид".formatted(title));
            }
            if (room.kind() == RoomKind.TOWER && !validCircle(room.shape())) {
                throw badRequest("Окружность башни «%s» задана неверно или выходит за участок".formatted(title));
            }
            requireLevel(levels, room.level());
            if (room.cells().isEmpty()) {
                throw badRequest("В «%s» нет ни одной клетки".formatted(title));
            }
            Map<Cell, Room> floor = floors.computeIfAbsent(room.level(), level -> new HashMap<>());
            for (Cell cell : room.cells()) {
                requireCellOnGrid(cell);
                Room other = floor.putIfAbsent(cell, room);
                if (other != null) {
                    throw badRequest("Клетка (%d, %d) занята и «%s», и «%s»"
                            .formatted(cell.x(), cell.y(), title(other), title));
                }
            }
        }
        return floors;
    }

    private static void validateArea(List<Room> rooms,
                                     Map<UUID, FacilitySpace> facilities,
                                     Map<UUID, String> names) {
        Map<UUID, Integer> area = new HashMap<>();
        for (Room room : rooms) {
            if (room.facilityId() == null) {
                continue;
            }
            if (!facilities.containsKey(room.facilityId())) {
                throw badRequest("На плане сооружение, которого нет в бастионе");
            }
            area.merge(room.facilityId(), room.cells().size(), Integer::sum);
        }
        area.forEach((facilityId, squares) -> {
            FacilitySpace space = facilities.get(facilityId);
            if (squares > space.getSquares()) {
                throw badRequest("«%s» занимает %d клеток, а его пространство (%s) — не больше %d"
                        .formatted(names.getOrDefault(facilityId, "Сооружение"), squares,
                                space.getName().toLowerCase(), space.getSquares()));
            }
        });
    }

    /** Дверь или окно стоит на ребре, у которого хотя бы с одной стороны комната, и ребро не занято. */
    private static void requireEdge(Map<Integer, Map<Cell, Room>> floors, Set<String> edges,
                                    int level, int x, int y, Side side, String what) {
        if (side == null) {
            throw badRequest("%s без стороны клетки".formatted(what));
        }
        Map<Cell, Room> floor = floors.getOrDefault(level, Map.of());
        if (PlanGeometry.edgeCells(x, y, side).stream().noneMatch(floor::containsKey)) {
            throw badRequest("%s в (%d, %d) стоит не у комнаты".formatted(what, x, y));
        }
        if (!edges.add(level + ":" + PlanGeometry.edgeKey(x, y, side))) {
            throw badRequest("В стене у (%d, %d) две двери или окна в одном месте".formatted(x, y));
        }
    }

    private static void validateTowerOpenings(List<TowerOpening> openings, List<Room> rooms) {
        Map<String, Room> towers = rooms.stream()
                .filter(room -> room.kind() == RoomKind.TOWER)
                .collect(Collectors.toMap(Room::id, room -> room));
        Set<String> taken = new HashSet<>();
        for (TowerOpening opening : openings) {
            Room tower = towers.get(opening.roomId());
            if (tower == null) {
                throw badRequest("Проём в стене башни, которой нет на плане");
            }
            double steps = opening.angle() / TOWER_ANGLE_STEP;
            if (opening.angle() < 0 || opening.angle() >= 360 || Math.abs(steps - Math.rint(steps)) > 1e-9) {
                throw badRequest("Проём в «%s» стоит под неверным углом".formatted(title(tower)));
            }
            if (opening.type() == null || opening.type() == OpeningType.DOOR && opening.kind() == null) {
                throw badRequest("У проёма в «%s» не указан вид".formatted(title(tower)));
            }
            if (!taken.add(opening.roomId() + ":" + Math.rint(steps))) {
                throw badRequest("В стене «%s» два проёма в одном месте".formatted(title(tower)));
            }
        }
    }

    private static void requireObjectId(Set<String> ids, String id) {
        if (!StringUtils.hasText(id) || !ids.add(id)) {
            throw badRequest("У каждой лестницы и колонны должен быть свой идентификатор");
        }
    }

    private static void validateStair(Stair stair, Set<Integer> levels) {
        requireLevel(levels, stair.level());
        if (stair.direction() == null || stair.kind() == null) {
            throw badRequest("У лестницы не указан вид или направление");
        }
        if (stair.toLevel() == stair.level() || !levels.contains(stair.toLevel())) {
            throw badRequest("Лестница ведёт на этаж, которого нет");
        }
        if (stair.width() < 1 || stair.height() < 1
                || stair.width() > MAX_STAIR_SIDE || stair.height() > MAX_STAIR_SIDE
                || !onGrid(stair.x(), stair.y()) || !onGrid(stair.x() + stair.width(), stair.y() + stair.height())) {
            throw badRequest("Лестница задана неверно или выходит за участок");
        }
    }

    private static boolean validCircle(Circle circle) {
        return circle != null && halfStep(circle.cx()) && halfStep(circle.cy()) && halfStep(circle.r())
                && circle.r() >= MIN_TOWER_RADIUS && circle.r() <= MAX_TOWER_RADIUS
                && onGrid(circle.cx() - circle.r(), circle.cy() - circle.r())
                && onGrid(circle.cx() + circle.r(), circle.cy() + circle.r());
    }

    private static void requireLevel(Set<Integer> levels, int level) {
        if (!levels.contains(level)) {
            throw badRequest("На плане нет этажа %d".formatted(level));
        }
    }

    private static void requireCellOnGrid(Cell cell) {
        if (cell == null || cell.x() < 0 || cell.y() < 0 || cell.x() >= GRID_SIZE || cell.y() >= GRID_SIZE) {
            throw badRequest("Клетка вне участка");
        }
    }

    private static boolean onGrid(double x, double y) {
        return x >= 0 && y >= 0 && x <= GRID_SIZE && y <= GRID_SIZE;
    }

    /** Значение конечное и кратно половине клетки. */
    private static boolean halfStep(double value) {
        return Double.isFinite(value) && Math.abs(value * 2 - Math.rint(value * 2)) <= 1e-9;
    }

    private static String title(Room room) {
        return Optional.ofNullable(room.name()).filter(StringUtils::hasText).orElse("Комната");
    }

    private static <T> List<T> list(List<T> values) {
        return values == null ? List.of() : values.stream().filter(Objects::nonNull).toList();
    }

    private static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }
}
