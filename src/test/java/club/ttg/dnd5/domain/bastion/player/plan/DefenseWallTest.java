package club.ttg.dnd5.domain.bastion.player.plan;

import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Cell;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Room;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.RoomKind;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefenseWallTest {

    /** Комната 3×3 в (5, 5). */
    private static Room hall() {
        List<Cell> cells = new ArrayList<>();
        for (int y = 5; y <= 7; y++) {
            for (int x = 5; x <= 7; x++) {
                cells.add(new Cell(x, y));
            }
        }
        return new Room("hall", "Зал", RoomKind.ROOM, 0, null, cells, null);
    }

    /** Кольцо стены вокруг зала, без указанных клеток. */
    private static List<Cell> ring(List<Cell> without) {
        List<Cell> cells = new ArrayList<>();
        for (int y = 4; y <= 8; y++) {
            for (int x = 4; x <= 8; x++) {
                Cell cell = new Cell(x, y);
                if ((x == 4 || x == 8 || y == 4 || y == 8) && !without.contains(cell)) {
                    cells.add(cell);
                }
            }
        }
        return cells;
    }

    private static PlanDocument plan(List<Room> rooms, List<Cell> walls, List<Cell> gates) {
        PlanDocument empty = PlanDocument.empty();
        return new PlanDocument(empty.levels(), rooms, empty.doors(), empty.windows(),
                empty.towerOpenings(), empty.stairs(), empty.columns(), walls, empty.furniture(), gates);
    }

    @Test
    void closedRingEnclosesBastion() {
        assertTrue(PlanRules.isEnclosed(plan(List.of(hall()), ring(List.of()), List.of())));
    }

    @Test
    void diagonalJointAndGatesStayClosed() {
        List<Cell> corners = List.of(new Cell(4, 4), new Cell(8, 4), new Cell(4, 8), new Cell(8, 8));

        assertTrue(PlanRules.isEnclosed(plan(List.of(hall()), ring(corners), List.of(new Cell(6, 4)))));
    }

    @Test
    void gapOrNoRoomsIsNotEnclosed() {
        assertFalse(PlanRules.isEnclosed(plan(List.of(hall()), ring(List.of(new Cell(6, 4))), List.of())));
        assertFalse(PlanRules.isEnclosed(plan(List.of(), ring(List.of()), List.of())));
        assertFalse(PlanRules.isEnclosed(plan(List.of(hall()), List.of(), List.of())));
    }

    @Test
    void wallCannotGrowPastBuiltBlocks() {
        PlanDocument walled = plan(List.of(), ring(List.of()), List.of());

        assertTrue(PlayerBastionPlanService.exceedsWallBlocks(null, walled, 10));
        assertFalse(PlayerBastionPlanService.exceedsWallBlocks(null, walled, 16));
    }

    /** Стена, нарисованная до покупок, остаётся: её можно убирать, но не наращивать. */
    @Test
    void wallDrawnBeforeBlocksIsKept() {
        PlanDocument saved = plan(List.of(), ring(List.of()), List.of());
        PlanDocument shorter = plan(List.of(), ring(List.of(new Cell(6, 4))), List.of());
        List<Cell> longer = new ArrayList<>(ring(List.of()));
        longer.add(new Cell(0, 0));

        assertFalse(PlayerBastionPlanService.exceedsWallBlocks(saved, saved, 0));
        assertFalse(PlayerBastionPlanService.exceedsWallBlocks(saved, shorter, 0));
        assertTrue(PlayerBastionPlanService.exceedsWallBlocks(saved, plan(List.of(), longer, List.of()), 0));
    }
}
