package club.ttg.dnd5.domain.bastion.player.plan;

import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Building;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Cell;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Door;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.FacilityCell;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Floor;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Outline;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Passage;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Stair;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Window;
import club.ttg.dnd5.exception.ApiException;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

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
 *     <li>площадь каждого сооружения — сумма его клеток по всем этажам и корпусам — не
 *     больше его пространства (тесное 4, вместительное 16, просторное 36);</li>
 *     <li>клетки сооружений лежат внутри контура своего корпуса и не повторяются на этаже;</li>
 *     <li>на плане только сооружения этого бастиона;</li>
 *     <li>двери, окна и лестницы стоят внутри корпуса, лестница ведёт на существующий этаж;</li>
 *     <li>размеры плана ограничены, чтобы документ не разрастался бесконечно.</li>
 * </ul></p>
 */
@UtilityClass
public class PlanRules {
    /** Сторона участка в клетках: 200 клеток — 1000 футов. */
    public static final int GRID_SIZE = 200;
    public static final int MAX_BUILDINGS = 40;
    public static final int MAX_FLOORS = 10;
    public static final int MIN_LEVEL = -3;
    public static final int MAX_LEVEL = 9;
    public static final int MAX_POLYGON_POINTS = 64;
    public static final int MAX_LOOSE_CELLS = 4_000;
    public static final int MAX_NAME_LENGTH = 100;

    /**
     * Проверяет план.
     *
     * @param document   план
     * @param facilities сооружения бастиона: id → пространство
     * @param names      названия сооружений для сообщений об ошибке
     * @throws ApiException 400 с объяснением
     */
    public static void validate(PlanDocument document,
                                Map<UUID, FacilitySpace> facilities,
                                Map<UUID, String> names) {
        List<Building> buildings = list(document.buildings());
        if (buildings.size() > MAX_BUILDINGS) {
            throw badRequest("На плане не больше %d корпусов".formatted(MAX_BUILDINGS));
        }

        Map<UUID, Integer> area = new HashMap<>();
        Set<String> ids = new HashSet<>();
        for (Building building : buildings) {
            if (!StringUtils.hasText(building.id()) || !ids.add(building.id())) {
                throw badRequest("У каждого корпуса должен быть свой идентификатор");
            }
            if (building.name() != null && building.name().length() > MAX_NAME_LENGTH) {
                throw badRequest("Название корпуса длиннее %d символов".formatted(MAX_NAME_LENGTH));
            }
            validateBuilding(building, facilities, area);
        }

        area.forEach((facilityId, squares) -> {
            FacilitySpace space = facilities.get(facilityId);
            if (squares > space.getSquares()) {
                throw badRequest("«%s» занимает %d клеток, а его пространство (%s) — не больше %d"
                        .formatted(names.getOrDefault(facilityId, "Сооружение"), squares,
                                space.getName().toLowerCase(), space.getSquares()));
            }
        });

        int looseCells = list(document.walls()).size() + list(document.passages()).stream()
                .mapToInt(passage -> list(passage.cells()).size())
                .sum();
        if (looseCells > MAX_LOOSE_CELLS) {
            throw badRequest("Слишком много клеток переходов и стен: больше %d".formatted(MAX_LOOSE_CELLS));
        }
        for (Passage passage : list(document.passages())) {
            if (passage.kind() == null) {
                throw badRequest("У перехода не указан вид");
            }
            requireLevel(passage.level());
            list(passage.cells()).forEach(PlanRules::requireCellOnGrid);
        }
        list(document.walls()).forEach(PlanRules::requireCellOnGrid);
    }

    private static void validateBuilding(Building building,
                                         Map<UUID, FacilitySpace> facilities,
                                         Map<UUID, Integer> area) {
        String title = Optional.ofNullable(building.name()).filter(StringUtils::hasText).orElse("Корпус");
        Outline outline = requireOutline(building.outline(), title);
        Set<Cell> inside = PlanGeometry.cellsInside(outline);

        List<Floor> floors = list(building.floors());
        if (floors.isEmpty() || floors.size() > MAX_FLOORS) {
            throw badRequest("У «%s» должно быть от 1 до %d этажей".formatted(title, MAX_FLOORS));
        }
        Set<Integer> levels = floors.stream().map(Floor::level).collect(Collectors.toSet());
        if (levels.size() != floors.size()) {
            throw badRequest("У «%s» два этажа на одном уровне".formatted(title));
        }

        for (Floor floor : floors) {
            requireLevel(floor.level());
            Set<Cell> taken = new HashSet<>();
            for (FacilityCell cell : list(floor.cells())) {
                Cell position = new Cell(cell.x(), cell.y());
                if (!inside.contains(position)) {
                    throw badRequest("Клетка сооружения (%d, %d) лежит вне контура «%s»"
                            .formatted(cell.x(), cell.y(), title));
                }
                if (!taken.add(position)) {
                    throw badRequest("Клетка (%d, %d) в «%s» занята дважды".formatted(cell.x(), cell.y(), title));
                }
                if (cell.facilityId() == null || !facilities.containsKey(cell.facilityId())) {
                    throw badRequest("На плане сооружение, которого нет в бастионе");
                }
                area.merge(cell.facilityId(), 1, Integer::sum);
            }
            validateOpenings(floor, inside, title);
            for (Stair stair : list(floor.stairs())) {
                requireInside(inside, stair.x(), stair.y(), title, "Лестница");
                if (stair.toLevel() == floor.level() || !levels.contains(stair.toLevel())) {
                    throw badRequest("Лестница в «%s» ведёт на этаж, которого нет".formatted(title));
                }
            }
        }
    }

    private static void validateOpenings(Floor floor, Set<Cell> inside, String title) {
        Set<String> doors = new HashSet<>();
        for (Door door : list(floor.doors())) {
            requireInside(inside, door.x(), door.y(), title, "Дверь");
            if (door.side() == null || door.kind() == null
                    || !doors.add(door.x() + ":" + door.y() + ":" + door.side())) {
                throw badRequest("Дверь в «%s» указана неверно или дважды".formatted(title));
            }
        }
        Set<String> windows = new HashSet<>();
        for (Window window : list(floor.windows())) {
            requireInside(inside, window.x(), window.y(), title, "Окно");
            if (window.side() == null || !windows.add(window.x() + ":" + window.y() + ":" + window.side())) {
                throw badRequest("Окно в «%s» указано неверно или дважды".formatted(title));
            }
        }
    }

    private static Outline requireOutline(Outline outline, String title) {
        if (outline == null || outline.type() == null) {
            throw badRequest("У «%s» нет контура".formatted(title));
        }
        boolean valid = switch (outline.type()) {
            case CIRCLE -> allHalfSteps(outline.cx(), outline.cy(), outline.r())
                    && outline.r() >= 0.5 && outline.r() <= GRID_SIZE / 2.0
                    && onGrid(outline.cx() - outline.r(), outline.cy() - outline.r())
                    && onGrid(outline.cx() + outline.r(), outline.cy() + outline.r());
            case RECTANGLE -> allHalfSteps(outline.x(), outline.y(), outline.width(), outline.height())
                    && outline.width() >= 1 && outline.height() >= 1
                    && onGrid(outline.x(), outline.y())
                    && onGrid(outline.x() + outline.width(), outline.y() + outline.height());
            case POLYGON -> {
                List<List<Double>> points = list(outline.points());
                yield points.size() >= 3 && points.size() <= MAX_POLYGON_POINTS
                        && points.stream().allMatch(point -> point != null && point.size() == 2
                        && allHalfSteps(point.get(0), point.get(1)) && onGrid(point.get(0), point.get(1)));
            }
        };
        if (!valid) {
            throw badRequest("Контур «%s» задан неверно или выходит за участок".formatted(title));
        }
        return outline;
    }

    private static void requireInside(Set<Cell> inside, int x, int y, String title, String what) {
        if (!inside.contains(new Cell(x, y))) {
            throw badRequest("%s в «%s» стоит вне контура".formatted(what, title));
        }
    }

    private static void requireCellOnGrid(Cell cell) {
        if (cell == null || cell.x() < 0 || cell.y() < 0 || cell.x() >= GRID_SIZE || cell.y() >= GRID_SIZE) {
            throw badRequest("Клетка вне участка");
        }
    }

    private static void requireLevel(int level) {
        if (level < MIN_LEVEL || level > MAX_LEVEL) {
            throw badRequest("Этаж должен быть от %d до %d".formatted(MIN_LEVEL, MAX_LEVEL));
        }
    }

    private static boolean onGrid(double x, double y) {
        return x >= 0 && y >= 0 && x <= GRID_SIZE && y <= GRID_SIZE;
    }

    /** Значения заданы и кратны половине клетки. */
    private static boolean allHalfSteps(Double... values) {
        for (Double value : values) {
            if (value == null || !Double.isFinite(value) || Math.abs(value * 2 - Math.rint(value * 2)) > 1e-9) {
                return false;
            }
        }
        return true;
    }

    private static <T> List<T> list(List<T> values) {
        return values == null ? List.of() : values.stream().filter(Objects::nonNull).toList();
    }

    private static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }
}
