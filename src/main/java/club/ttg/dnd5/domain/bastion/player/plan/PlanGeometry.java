package club.ttg.dnd5.domain.bastion.player.plan;

import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Cell;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Outline;
import lombok.experimental.UtilityClass;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Геометрия плана: какие клетки входят в контур корпуса.
 *
 * <p>Правило одно для сервера и редактора: клетка входит в корпус, если её центр
 * ({@code x + 0.5, y + 0.5}) лежит внутри контура. Круглая башня радиусом 2 клетки
 * (10 футов) даёт так 12 клеток на этаж. Редактор повторяет это правило в
 * {@code plan/geometry.ts} — разойдутся, и счётчики площади на экране перестанут
 * совпадать с проверкой при сохранении.</p>
 */
@UtilityClass
public class PlanGeometry {
    /** Точность сравнения: центр ровно на окружности считается внутри. */
    private static final double EPSILON = 1e-9;

    /** Клетки, центр которых лежит внутри контура. */
    public static Set<Cell> cellsInside(Outline outline) {
        Bounds bounds = bounds(outline);
        Set<Cell> cells = new LinkedHashSet<>();
        for (int y = (int) Math.floor(bounds.minY()); y < Math.ceil(bounds.maxY()); y++) {
            for (int x = (int) Math.floor(bounds.minX()); x < Math.ceil(bounds.maxX()); x++) {
                if (contains(outline, x + 0.5, y + 0.5)) {
                    cells.add(new Cell(x, y));
                }
            }
        }
        return cells;
    }

    /** Лежит ли точка внутри контура. */
    public static boolean contains(Outline outline, double px, double py) {
        return switch (outline.type()) {
            case CIRCLE -> {
                double dx = px - outline.cx();
                double dy = py - outline.cy();
                yield dx * dx + dy * dy <= outline.r() * outline.r() + EPSILON;
            }
            case RECTANGLE -> px >= outline.x() && px <= outline.x() + outline.width()
                    && py >= outline.y() && py <= outline.y() + outline.height();
            case POLYGON -> polygonContains(outline.points(), px, py);
        };
    }

    /** Прямоугольник, в который вписан контур. */
    public static Bounds bounds(Outline outline) {
        return switch (outline.type()) {
            case CIRCLE -> new Bounds(outline.cx() - outline.r(), outline.cy() - outline.r(),
                    outline.cx() + outline.r(), outline.cy() + outline.r());
            case RECTANGLE -> new Bounds(outline.x(), outline.y(),
                    outline.x() + outline.width(), outline.y() + outline.height());
            case POLYGON -> {
                double minX = Double.MAX_VALUE;
                double minY = Double.MAX_VALUE;
                double maxX = -Double.MAX_VALUE;
                double maxY = -Double.MAX_VALUE;
                for (List<Double> point : outline.points()) {
                    minX = Math.min(minX, point.get(0));
                    minY = Math.min(minY, point.get(1));
                    maxX = Math.max(maxX, point.get(0));
                    maxY = Math.max(maxY, point.get(1));
                }
                yield new Bounds(minX, minY, maxX, maxY);
            }
        };
    }

    /** Чётно-нечётное правило (луч вправо): точка внутри, если луч пересекает контур нечётное число раз. */
    private static boolean polygonContains(List<List<Double>> points, double px, double py) {
        boolean inside = false;
        for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            double xi = points.get(i).get(0);
            double yi = points.get(i).get(1);
            double xj = points.get(j).get(0);
            double yj = points.get(j).get(1);
            boolean crosses = (yi > py) != (yj > py)
                    && px < (xj - xi) * (py - yi) / (yj - yi) + xi;
            if (crosses) {
                inside = !inside;
            }
        }
        return inside;
    }

    public record Bounds(double minX, double minY, double maxX, double maxY) {
    }
}
