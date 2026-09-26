package club.ttg.dnd5.domain.bastion.player.plan;

import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Building;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.BuildingKind;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Cell;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.FacilityCell;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Floor;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Outline;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.OutlineType;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Stair;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanRulesTest {
    private final UUID bedroom = UUID.randomUUID();
    private final UUID library = UUID.randomUUID();
    private final Map<UUID, FacilitySpace> spaces = Map.of(bedroom, FacilitySpace.CRAMPED, library, FacilitySpace.ROOMY);
    private final Map<UUID, String> names = Map.of(bedroom, "Спальня", library, "Библиотека");

    /** Круглая башня радиусом 10 футов (2 клетки) — 12 клеток: квадрат 4×4 без углов. */
    @Test
    void roundTowerOfTenFeetRadiusHasTwelveCells() {
        Set<Cell> cells = PlanGeometry.cellsInside(circle(5, 6, 2));

        assertEquals(12, cells.size());
        assertTrue(cells.contains(new Cell(4, 5)));
        assertFalse(cells.contains(new Cell(3, 4)), "угол квадрата за окружностью");
    }

    @Test
    void polygonUsesCellCenters() {
        Outline triangle = new Outline(OutlineType.POLYGON, null, null, null, null, null, null, null,
                List.of(List.of(0.0, 0.0), List.of(4.0, 0.0), List.of(0.0, 4.0)));

        Set<Cell> cells = PlanGeometry.cellsInside(triangle);

        assertTrue(cells.contains(new Cell(0, 0)));
        assertTrue(cells.contains(new Cell(1, 1)));
        assertFalse(cells.contains(new Cell(3, 3)));
    }

    /** Библиотека разложена по двум этажам башни: 12 + 4 = 16 — ровно вместительное пространство. */
    @Test
    void facilityMaySpanFloors() {
        List<Cell> towerCells = new ArrayList<>(PlanGeometry.cellsInside(circle(5, 6, 2)));
        Building tower = new Building("tower", "Северная башня", BuildingKind.TOWER, circle(5, 6, 2), List.of(
                floor(0, cells(towerCells, library, 12), List.of(new Stair(4, 5, 1))),
                floor(1, cells(towerCells, library, 4), List.of())));

        assertDoesNotThrow(() -> PlanRules.validate(plan(tower), spaces, names));
    }

    @Test
    void areaAboveSpaceIsRejected() {
        Building keep = new Building("keep", "Донжон", BuildingKind.KEEP, rectangle(0, 0, 3, 2), List.of(
                floor(0, List.of(
                        new FacilityCell(0, 0, bedroom), new FacilityCell(1, 0, bedroom),
                        new FacilityCell(2, 0, bedroom), new FacilityCell(0, 1, bedroom),
                        new FacilityCell(1, 1, bedroom)), List.of())));

        ApiException error = assertThrows(ApiException.class, () -> PlanRules.validate(plan(keep), spaces, names));

        assertTrue(error.getMessage().contains("Спальня"), error.getMessage());
        assertTrue(error.getMessage().contains("не больше 4"), error.getMessage());
    }

    @Test
    void cellOutsideOutlineIsRejected() {
        Building keep = new Building("keep", "Донжон", BuildingKind.KEEP, rectangle(0, 0, 2, 2), List.of(
                floor(0, List.of(new FacilityCell(5, 5, bedroom)), List.of())));

        assertRejected(plan(keep), "вне контура");
    }

    @Test
    void unknownFacilityIsRejected() {
        Building keep = new Building("keep", "Донжон", BuildingKind.KEEP, rectangle(0, 0, 2, 2), List.of(
                floor(0, List.of(new FacilityCell(0, 0, UUID.randomUUID())), List.of())));

        assertRejected(plan(keep), "нет в бастионе");
    }

    @Test
    void stairMustLeadToExistingFloor() {
        Building keep = new Building("keep", "Донжон", BuildingKind.KEEP, rectangle(0, 0, 2, 2), List.of(
                floor(0, List.of(), List.of(new Stair(0, 0, 3)))));

        assertRejected(plan(keep), "Лестница");
    }

    @Test
    void outlineOutsideGridIsRejected() {
        Building keep = new Building("keep", "Донжон", BuildingKind.KEEP, circle(1, 1, 3), List.of(
                floor(0, List.of(), List.of())));

        assertRejected(plan(keep), "Контур");
    }

    @Test
    void cellsOfRemovedFacilitiesAreDropped() {
        UUID removed = UUID.randomUUID();
        Building keep = new Building("keep", "Донжон", BuildingKind.KEEP, rectangle(0, 0, 2, 2), List.of(
                floor(0, List.of(new FacilityCell(0, 0, removed), new FacilityCell(1, 0, bedroom)), List.of())));

        PlanDocument cleaned = PlayerBastionPlanService.withoutMissingFacilities(plan(keep), spaces);

        assertEquals(List.of(new FacilityCell(1, 0, bedroom)),
                cleaned.buildings().getFirst().floors().getFirst().cells());
    }

    private void assertRejected(PlanDocument document, String messagePart) {
        ApiException error = assertThrows(ApiException.class, () -> PlanRules.validate(document, spaces, names));
        assertTrue(error.getMessage().contains(messagePart), error.getMessage());
    }

    private static PlanDocument plan(Building building) {
        return new PlanDocument(List.of(building), List.of(), List.of());
    }

    private static Floor floor(int level, List<FacilityCell> cells, List<Stair> stairs) {
        return new Floor(level, cells, List.of(), List.of(), stairs);
    }

    private static List<FacilityCell> cells(List<Cell> source, UUID facilityId, int count) {
        return source.stream().limit(count).map(cell -> new FacilityCell(cell.x(), cell.y(), facilityId)).toList();
    }

    private static Outline circle(double cx, double cy, double r) {
        return new Outline(OutlineType.CIRCLE, cx, cy, r, null, null, null, null, null);
    }

    private static Outline rectangle(double x, double y, double width, double height) {
        return new Outline(OutlineType.RECTANGLE, null, null, null, x, y, width, height, null);
    }
}
