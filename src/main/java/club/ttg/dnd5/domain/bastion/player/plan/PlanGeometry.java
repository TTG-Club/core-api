package club.ttg.dnd5.domain.bastion.player.plan;

import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Cell;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Circle;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Side;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Геометрия плана.
 *
 * <p>Клетки башни — те, чей центр ({@code x + 0.5, y + 0.5}) лежит внутри окружности. Круглая
 * башня радиусом 2 клетки (10 футов) даёт так 12 клеток. Редактор повторяет это правило в
 * {@code plan-geometry.ts} — разойдутся, и счётчики площади на экране перестанут совпадать
 * с проверкой при сохранении.</p>
 */
@UtilityClass
public class PlanGeometry {
    /** Точность сравнения: центр ровно на окружности считается внутри. */
    private static final double EPSILON = 1e-9;

    /** Клетки, центр которых лежит внутри окружности, в порядке строк. */
    public static List<Cell> cellsInside(Circle circle) {
        List<Cell> cells = new ArrayList<>();
        for (int y = (int) Math.floor(circle.cy() - circle.r()); y < Math.ceil(circle.cy() + circle.r()); y++) {
            for (int x = (int) Math.floor(circle.cx() - circle.r()); x < Math.ceil(circle.cx() + circle.r()); x++) {
                double dx = x + 0.5 - circle.cx();
                double dy = y + 0.5 - circle.cy();
                if (dx * dx + dy * dy <= circle.r() * circle.r() + EPSILON) {
                    cells.add(new Cell(x, y));
                }
            }
        }
        return cells;
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
