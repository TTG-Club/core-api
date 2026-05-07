package club.ttg.dnd5.domain.initiative.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EncounterDifficulty {
    private long baseXp;
    private long adjustedXp;
    private int enemyCount;
    private int playerCount;
    private Thresholds thresholds = new Thresholds();
    private EncounterDifficultyLevel difficulty = EncounterDifficultyLevel.TRIVIAL;

    @Getter
    @Setter
    public static class Thresholds {
        private long easy;
        private long medium;
        private long hard;
        private long deadly;
    }
}
