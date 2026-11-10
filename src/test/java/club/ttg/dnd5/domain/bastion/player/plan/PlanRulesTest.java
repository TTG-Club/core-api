package club.ttg.dnd5.domain.bastion.player.plan;

import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Cell;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Circle;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Column;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Door;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.DoorKind;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Furniture;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.FurnitureKind;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.OpeningType;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Room;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.RoomKind;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Side;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Stair;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.StairKind;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.TowerOpening;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Window;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        List<Cell> cells = PlanGeometry.cellsInside(new Circle(5, 6, 2));

        assertEquals(12, cells.size());
        assertTrue(cells.contains(new Cell(4, 5)));
        assertFalse(cells.contains(new Cell(3, 4)), "угол квадрата за окружностью");
    }

    @Test
    void edgeIsTheSameFromBothSides() {
        assertEquals(PlanGeometry.edgeKey(3, 4, Side.S), PlanGeometry.edgeKey(3, 5, Side.N));
        assertEquals(PlanGeometry.edgeKey(3, 4, Side.E), PlanGeometry.edgeKey(4, 4, Side.W));
    }

    /** Сервер не верит клеткам башни от клиента и считает их по окружности. */
    @Test
    void towerCellsAreRecomputed() {
        Room tower = new Room("tower", "Башня", RoomKind.TOWER, 0, null, List.of(new Cell(0, 0)),
                new Circle(5, 6, 2));

        PlanDocument normalized = PlanRules.normalize(plan(List.of(0), tower), spaces);

        assertEquals(12, normalized.rooms().getFirst().cells().size());
    }

    /** Библиотека в двух комнатах на разных этажах: 12 + 4 = 16 — ровно вместительное пространство. */
    @Test
    void facilityMaySpanFloors() {
        Room tower = new Room("tower", "Башня", RoomKind.TOWER, 0, library, List.of(), new Circle(5, 6, 2));
        Room study = room("study", 1, library, rect(3, 4, 2, 2));
        PlanDocument document = PlanRules.normalize(plan(List.of(0, 1), tower, study), spaces);

        assertDoesNotThrow(() -> PlanRules.validate(document, spaces, names));
    }

    @Test
    void areaAboveSpaceIsRejected() {
        PlanDocument document = plan(List.of(0), room("bed", 0, bedroom, rect(0, 0, 3, 2)));

        ApiException error = assertThrows(ApiException.class, () -> PlanRules.validate(document, spaces, names));

        assertTrue(error.getMessage().contains("Спальня"), error.getMessage());
        assertTrue(error.getMessage().contains("не больше 4"), error.getMessage());
    }

    /** Пересечение комнат не отказ, а правило: клетка остаётся за той, что раньше в списке. */
    @Test
    void overlappingCellsStayWithFirstRoom() {
        PlanDocument document = PlanRules.normalize(plan(List.of(0),
                room("hall", 0, null, rect(0, 0, 3, 3)),
                room("kitchen", 0, null, rect(2, 2, 2, 2))), spaces);

        assertEquals(9, document.rooms().get(0).cells().size());
        assertEquals(3, document.rooms().get(1).cells().size());
        assertDoesNotThrow(() -> PlanRules.validate(document, spaces, names));
    }

    @Test
    void towerKeepsItsCellsAndSwallowedRoomDisappears() {
        Room closet = room("closet", 0, null, List.of(new Cell(4, 5)));
        Room tower = new Room("tower", "Башня", RoomKind.TOWER, 0, null, List.of(), new Circle(5, 6, 2));

        PlanDocument document = PlanRules.normalize(plan(List.of(0), closet, tower), spaces);

        assertEquals(1, document.rooms().size());
        assertEquals(12, document.rooms().getFirst().cells().size());
    }

    @Test
    void squareTowerKeepsItsCellsOverEarlierRoom() {
        Room hall = room("hall", 0, null, List.of(new Cell(4, 5), new Cell(5, 5)));
        Room tower = new Room("tower", "Башня", RoomKind.SQUARE_TOWER, 0, null,
                List.of(new Cell(5, 5), new Cell(6, 5), new Cell(5, 6), new Cell(6, 6)), null);

        PlanDocument document = PlanRules.normalize(plan(List.of(0), hall, tower), spaces);

        assertEquals(List.of(new Cell(4, 5)), document.rooms().get(0).cells());
        assertEquals(4, document.rooms().get(1).cells().size());
        assertNull(document.rooms().get(1).shape());
        assertDoesNotThrow(() -> PlanRules.validate(document, spaces, names));
    }

    @Test
    void stairsAndColumnsNeedUniqueIds() {
        PlanDocument base = plan(List.of(0, 1), room("hall", 0, null, rect(0, 0, 3, 3)));
        Stair stair = new Stair("same", 0, 1, 1, 1, 2, Side.N, 1, StairKind.STRAIGHT);
        PlanDocument clash = new PlanDocument(base.levels(), base.rooms(), List.of(), List.of(), List.of(),
                List.of(stair), List.of(new Column("same", 0, 1, 1)), List.of(), List.of(), List.of());

        assertThrows(ApiException.class, () -> PlanRules.validate(clash, spaces, names));
    }

    @Test
    void sameCellsOnDifferentFloorsAreFine() {
        PlanDocument document = plan(List.of(0, 1),
                room("hall", 0, null, rect(0, 0, 3, 3)),
                room("chambers", 1, null, rect(0, 0, 3, 3)));

        assertDoesNotThrow(() -> PlanRules.validate(document, spaces, names));
    }

    @Test
    void removedFacilityLeavesPlainRoom() {
        UUID removed = UUID.randomUUID();

        PlanDocument normalized = PlanRules.normalize(plan(List.of(0), room("hall", 0, removed, rect(0, 0, 2, 2))),
                spaces);

        assertNull(normalized.rooms().getFirst().facilityId());
        assertDoesNotThrow(() -> PlanRules.validate(normalized, spaces, names));
    }

    @Test
    void doorsAndWindowsStandOnRoomEdges() {
        Room hall = room("hall", 0, null, rect(0, 0, 3, 3));
        PlanDocument valid = withObjects(plan(List.of(0), hall),
                List.of(new Door(0, 0, 0, Side.N, DoorKind.DOOR)),
                List.of(new Window(0, 2, 1, Side.E)));
        PlanDocument farAway = withObjects(plan(List.of(0), hall),
                List.of(new Door(0, 10, 10, Side.N, DoorKind.DOOR)), List.of());
        PlanDocument twice = withObjects(plan(List.of(0), hall),
                List.of(new Door(0, 0, 2, Side.S, DoorKind.DOOR)),
                List.of(new Window(0, 0, 3, Side.N)));

        assertDoesNotThrow(() -> PlanRules.validate(valid, spaces, names));
        assertThrows(ApiException.class, () -> PlanRules.validate(farAway, spaces, names));
        assertThrows(ApiException.class, () -> PlanRules.validate(twice, spaces, names));
    }

    @Test
    void stairsLeadToExistingFloor() {
        PlanDocument base = plan(List.of(0, 1), room("hall", 0, null, rect(0, 0, 3, 3)));
        Stair up = new Stair("stair", 0, 1, 1, 1, 2, Side.N, 1, StairKind.STRAIGHT);
        Stair nowhere = new Stair("stair", 0, 1, 1, 1, 2, Side.N, 2, StairKind.STRAIGHT);

        assertDoesNotThrow(() -> PlanRules.validate(withStairs(base, up), spaces, names));
        assertThrows(ApiException.class, () -> PlanRules.validate(withStairs(base, nowhere), spaces, names));
    }

    @Test
    void towerOpeningsSnapToAngleStep() {
        Room tower = new Room("tower", "Башня", RoomKind.TOWER, 0, null, List.of(), new Circle(5, 6, 2));
        PlanDocument base = PlanRules.normalize(plan(List.of(0), tower), spaces);

        assertDoesNotThrow(() -> PlanRules.validate(withOpening(base,
                new TowerOpening("tower", 22.5, OpeningType.WINDOW, null)), spaces, names));
        assertThrows(ApiException.class, () -> PlanRules.validate(withOpening(base,
                new TowerOpening("tower", 10, OpeningType.WINDOW, null)), spaces, names));
        assertThrows(ApiException.class, () -> PlanRules.validate(withOpening(base,
                new TowerOpening("missing", 0, OpeningType.DOOR, DoorKind.DOOR)), spaces, names));
    }

    @Test
    void octagonTowerKeepsCellsInsidePolygon() {
        List<Cell> cells = PlanGeometry.cellsInside(new Circle(5, 6, 2, 8));

        assertEquals(12, cells.size());
        assertFalse(cells.contains(new Cell(3, 4)));
        assertTrue(cells.contains(new Cell(3, 5)));
    }

    @Test
    void pentagonOpeningsStandOnSideMiddles() {
        Room tower = new Room("tower", "Башня", RoomKind.TOWER, 0, null, List.of(), new Circle(10, 10, 3, 5));
        PlanDocument base = PlanRules.normalize(plan(List.of(0), tower), spaces);

        assertDoesNotThrow(() -> PlanRules.validate(withOpening(base,
                new TowerOpening("tower", 18, OpeningType.WINDOW, null)), spaces, names));
        assertThrows(ApiException.class, () -> PlanRules.validate(withOpening(base,
                new TowerOpening("tower", 22.5, OpeningType.WINDOW, null)), spaces, names));
    }

    @Test
    void towerWithUnknownSidesIsRejected() {
        Room tower = new Room("tower", "Башня", RoomKind.TOWER, 0, null, rect(9, 9, 2, 2),
                new Circle(10, 10, 3, 6));
        PlanDocument document = PlanRules.normalize(plan(List.of(0), tower), spaces);

        assertThrows(ApiException.class, () -> PlanRules.validate(document, spaces, names));
    }

    @Test
    void firstFloorIsRequired() {
        PlanDocument document = plan(List.of(1), room("hall", 1, null, rect(0, 0, 2, 2)));

        assertThrows(ApiException.class, () -> PlanRules.validate(document, spaces, names));
    }

    @Test
    void emptyPlanIsValid() {
        PlanDocument document = PlanRules.normalize(PlanDocument.empty(), spaces);

        assertDoesNotThrow(() -> PlanRules.validate(document, spaces, names));
    }

    @Test
    void columnsSnapToHalfCells() {
        PlanDocument base = plan(List.of(0));

        assertDoesNotThrow(() -> PlanRules.validate(withColumn(base, new Column("column", 0, 2.5, 3)), spaces, names));
        assertThrows(ApiException.class,
                () -> PlanRules.validate(withColumn(base, new Column("column", 0, 2.3, 3)), spaces, names));
    }

    @Test
    void acceptsFurnitureInsideTheGrid() {
        PlanDocument base = plan(List.of(0), room("hall", 0, null, rect(0, 0, 3, 3)));
        PlanDocument withBed = withFurniture(base, new Furniture("bed", 0, 1, 1, FurnitureKind.BED, 90));

        assertDoesNotThrow(() -> PlanRules.validate(withBed, spaces, names));
    }

    @Test
    void rejectsFurnitureWithOddRotationOrOutsideTheGrid() {
        PlanDocument base = plan(List.of(0), room("hall", 0, null, rect(0, 0, 3, 3)));
        PlanDocument tilted = withFurniture(base, new Furniture("table", 0, 1, 1, FurnitureKind.TABLE, 45));
        PlanDocument outside = withFurniture(base,
                new Furniture("sofa", 0, PlanRules.GRID_SIZE - 1, 0, FurnitureKind.SOFA, 0));
        PlanDocument noLevel = withFurniture(base, new Furniture("chair", 3, 1, 1, FurnitureKind.CHAIR, 0));

        assertThrows(ApiException.class, () -> PlanRules.validate(tilted, spaces, names));
        assertThrows(ApiException.class, () -> PlanRules.validate(outside, spaces, names));
        assertThrows(ApiException.class, () -> PlanRules.validate(noLevel, spaces, names));
    }

    @Test
    void normalizeTurnsMissingFurnitureIntoEmptyList() {
        PlanDocument legacy = new PlanDocument(List.of(0), List.of(), null, null, null, null, null, null, null, null);

        PlanDocument normalized = PlanRules.normalize(legacy, Map.of());

        assertEquals(List.of(), normalized.furniture());
        assertEquals(List.of(), normalized.defenseGates());
    }

    @Test
    void normalizeKeepsGatesOnlyOnDefenseWall() {
        List<Cell> walls = List.of(new Cell(5, 5), new Cell(6, 5));
        PlanDocument document = new PlanDocument(List.of(0), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), walls, List.of(), List.of(new Cell(5, 5), new Cell(5, 5), new Cell(9, 9)));

        assertEquals(List.of(new Cell(5, 5)), PlanRules.normalize(document, Map.of()).defenseGates());
    }

    private static Room room(String id, int level, UUID facilityId, List<Cell> cells) {
        return new Room(id, id, RoomKind.ROOM, level, facilityId, cells, null);
    }

    private static List<Cell> rect(int x, int y, int width, int height) {
        List<Cell> cells = new ArrayList<>();
        for (int row = y; row < y + height; row++) {
            for (int column = x; column < x + width; column++) {
                cells.add(new Cell(column, row));
            }
        }
        return cells;
    }

    private static PlanDocument plan(List<Integer> levels, Room... rooms) {
        return new PlanDocument(levels, List.of(rooms), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of());
    }

    private static PlanDocument withObjects(PlanDocument document, List<Door> doors, List<Window> windows) {
        return new PlanDocument(document.levels(), document.rooms(), doors, windows, List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of());
    }

    private static PlanDocument withStairs(PlanDocument document, Stair stair) {
        return new PlanDocument(document.levels(), document.rooms(), List.of(), List.of(), List.of(),
                List.of(stair), List.of(), List.of(), List.of(), List.of());
    }

    private static PlanDocument withOpening(PlanDocument document, TowerOpening opening) {
        return new PlanDocument(document.levels(), document.rooms(), List.of(), List.of(), List.of(opening),
                List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private static PlanDocument withColumn(PlanDocument document, Column column) {
        return new PlanDocument(document.levels(), document.rooms(), List.of(), List.of(), List.of(), List.of(),
                List.of(column), List.of(), List.of(), List.of());
    }

    private static PlanDocument withFurniture(PlanDocument document, Furniture furniture) {
        return new PlanDocument(document.levels(), document.rooms(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(furniture), List.of());
    }
}
