package club.ttg.dnd5.domain.bastion.player.plan;

import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Cell;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Circle;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Side;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Геометрия плана.
 *
 * <p>Клетки башни — те, чей центр ({@code x + 0.5, y + 0.5}) лежит внутри окружности. Круглая
 * башня радиусом 2 клетки (10 футов) даёт так 12 клеток. Редактор повторяет это правило в
 * {@code plan-geometry.ts} — разойдутся, и счётчики площади на экране перестанут совпадать
 * с проверкой при сохранении.</p>
 *
 * <p>Многоугольная башня вписана в свою окружность: у пятиугольной вершина смотрит на
 * север, у восьмиугольной на север смотрит сторона. Проёмы в её стене стоят посередине
 * сторон, угол проёма — направление от центра на середину стороны.</p>
 */
@UtilityClass
public class PlanGeometry {
    /** Точность сравнения: центр ровно на окружности считается внутри. */
    private static final double EPSILON = 1e-9;

    /** Числа сторон многоугольных башен. */
    public static final Set<Integer> TOWER_SIDES = Set.of(5, 8);

    /** Клетки, центр которых лежит внутри окружности или многоугольника башни, в порядке строк. */
    public static List<Cell> cellsInside(Circle circle) {
        List<Double> normals = sideAngles(circle);
        double apothem = circle.sides() == null ? 0 : circle.r() * Math.cos(Math.PI / circle.sides());
        List<Cell> cells = new ArrayList<>();
        for (int y = (int) Math.floor(circle.cy() - circle.r()); y < Math.ceil(circle.cy() + circle.r()); y++) {
            for (int x = (int) Math.floor(circle.cx() - circle.r()); x < Math.ceil(circle.cx() + circle.r()); x++) {
                double dx = x + 0.5 - circle.cx();
                double dy = y + 0.5 - circle.cy();
                boolean inside = circle.sides() == null
                        ? dx * dx + dy * dy <= circle.r() * circle.r() + EPSILON
                        : normals.stream().allMatch(angle -> {
                            double radians = Math.toRadians(angle);
                            return dx * Math.cos(radians) + dy * Math.sin(radians) <= apothem + EPSILON;
                        });
                if (inside) {
                    cells.add(new Cell(x, y));
                }
            }
        }
        return cells;
    }

    /**
     * Углы на середины сторон многоугольной башни, градусы от востока по часовой в
     * {@code [0, 360)}; у круглой — пусто.
     */
    public static List<Double> sideAngles(Circle circle) {
        if (circle.sides() == null) {
            return List.of();
        }
        int sides = circle.sides();
        double step = 360.0 / sides;
        // Пятиугольник стоит вершиной на север, у восьмиугольника на север смотрит сторона.
        double first = sides % 2 == 1 ? -90 + step / 2 : -90;
        List<Double> angles = new ArrayList<>();
        for (int index = 0; index < sides; index++) {
            angles.add(((first + index * step) % 360 + 360) % 360);
        }
        return angles;
    }

    /**
     * Ребро в каноническом виде: нижняя сторона клетки — это верхняя сторона клетки под ней,
     * правая — левая сторона соседа справа. Так одно ребро записывается одинаково с обеих
     * сторон.
     *
     * @return ключ ребра {@code "x:y:N"} или {@code "x:y:W"}
     */
    public static String edgeKey(int x, int y, Side side) {
        return switch (side) {
            case N -> x + ":" + y + ":N";
            case S -> x + ":" + (y + 1) + ":N";
            case W -> x + ":" + y + ":W";
            case E -> (x + 1) + ":" + y + ":W";
        };
    }

    /** Две клетки по обе стороны ребра. */
    public static List<Cell> edgeCells(int x, int y, Side side) {
        return switch (side) {
            case N -> List.of(new Cell(x, y - 1), new Cell(x, y));
            case S -> List.of(new Cell(x, y), new Cell(x, y + 1));
            case W -> List.of(new Cell(x - 1, y), new Cell(x, y));
            case E -> List.of(new Cell(x, y), new Cell(x + 1, y));
        };
    }
}
