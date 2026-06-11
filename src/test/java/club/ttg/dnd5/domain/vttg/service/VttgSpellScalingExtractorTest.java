package club.ttg.dnd5.domain.vttg.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class VttgSpellScalingExtractorTest {
    private final VttgSpellScalingExtractor extractor = new VttgSpellScalingExtractor();

    @Test
    void extractsAdditionalDamagePerSlotLevel() {
        var result = extractor.extract(true,
                "Урон увеличивается на {@roll 1к6} за каждый уровень ячейки выше 3.");

        assertNotNull(result);
        assertEquals("1к6", result.getAdditionalDice());
        assertNull(result.getAdditionalTargets());
        assertEquals("Урон увеличивается на {@roll 1к6} за каждый уровень ячейки выше 3.",
                result.getDescription());
    }

    @Test
    void extractsAdditionalTargetsPerSlotLevel() {
        var result = extractor.extract(true,
                "Вы можете выбрать 1 дополнительную цель за каждый уровень ячейки выше 4.");

        assertNotNull(result);
        assertNull(result.getAdditionalDice());
        assertEquals(1, result.getAdditionalTargets());
    }

    @Test
    void keepsComplexScalingAsDescriptionOnly() {
        var result = extractor.extract(true,
                "Длительность увеличивается при использовании ячейки 6 уровня.");

        assertNotNull(result);
        assertNull(result.getAdditionalDice());
        assertNull(result.getAdditionalTargets());
        assertEquals("Длительность увеличивается при использовании ячейки 6 уровня.",
                result.getDescription());
    }

    @Test
    void createsEmptyScalingWhenSpellIsMarkedUpcastable() {
        var result = extractor.extract(true, null);

        assertNotNull(result);
        assertNull(result.getDescription());
    }

    @Test
    void ignoresSpellWithoutScalingData() {
        assertNull(extractor.extract(false, null));
    }
}
