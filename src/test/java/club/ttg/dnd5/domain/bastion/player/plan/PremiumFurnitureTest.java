package club.ttg.dnd5.domain.bastion.player.plan;

import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.Furniture;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.FurnitureKind;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PremiumFurnitureTest {

    private static PlanDocument withFurniture(Furniture... items) {
        PlanDocument empty = PlanDocument.empty();
        return new PlanDocument(empty.levels(), empty.rooms(), empty.doors(), empty.windows(),
                empty.towerOpenings(), empty.stairs(), empty.columns(), empty.defenseWalls(), List.of(items),
                empty.defenseGates());
    }

    @Test
    void newPremiumItemNeedsSubscription() {
        PlanDocument next = withFurniture(new Furniture("s", 0, 1, 1, FurnitureKind.STATUE, 0));

        assertTrue(PlayerBastionPlanService.hasNewPremiumFurniture(null, next));
        assertTrue(PlayerBastionPlanService.hasNewPremiumFurniture(PlanDocument.empty(), next));
    }

    @Test
    void movedPremiumItemAndPlainItemsDoNot() {
        PlanDocument saved = withFurniture(new Furniture("s", 0, 1, 1, FurnitureKind.FOUNTAIN, 0));
        PlanDocument moved = withFurniture(new Furniture("s", 0, 4, 2, FurnitureKind.FOUNTAIN, 90),
                new Furniture("t", 0, 8, 8, FurnitureKind.TABLE, 0));

        assertFalse(PlayerBastionPlanService.hasNewPremiumFurniture(saved, moved));
        assertFalse(PlayerBastionPlanService.hasNewPremiumFurniture(saved, PlanDocument.empty()));
    }
}
