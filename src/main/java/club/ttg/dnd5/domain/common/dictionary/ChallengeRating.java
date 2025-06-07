package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChallengeRating {
    CR_UNKNOWN("—", -1, 2),
    CR_0("0 ", 10, 2),
    CR_1_8("1/8", 25, 2),
    CR_1_4("1/4", 50, 2),
    CR_1_2("1/2", 100, 2),
    CR_1("1", 200, 2),
    CR_2("2", 450, 2),
    CR_3("3", 700, 2),
    CR_4("4", 1_100, 2),
    CR_5("5", 1_800, 3),
    CR_6("6", 2_300, 3),
    CR_7("7", 2_900, 3),
    CR_8("8", 3_900, 3),
    CR_9("9", 5_000, 4),
    CR_10("10", 5_900, 4),
    CR_11("11", 7_200, 4),
    CR_12("12", 8_400, 4),
    CR_13("13", 10_000, 5),
    CR_14("14", 11_500, 5),
    CR_15("15", 13_000, 5),
    CR_16("16", 15_000, 5),
    CR_17("17", 18_000, 6),
    CR_18("18", 20_000, 6),
    CR_19("19", 22_000, 6),
    CR_20("20", 25_000, 6),
    CR_21("21", 33_000, 7),
    CR_22("22", 41_000, 7),
    CR_23("23", 50_000, 7),
    CR_24("24", 62_000, 7),
    CR_25("25", 75_000, 8),
    CR_26("26", 90_000, 8),
    CR_27("27", 105_000, 8),
    CR_28("28", 120_000, 8),
    CR_29("29", 135_000, 9),
    CR_30("30", 155_000, 9);

    private final String name;
    private final long experience;
    private final int proficiencyBonus;

    public static String getCr(long experience) {
        for (var cr : values()) {
            if (cr.experience == experience) {
                return cr.name;
            }
        }
        return CR_UNKNOWN.name;
    }

    public static int getPb(long experience) {
        for (var cr : values()) {
            if (cr.experience == experience) {
                return cr.proficiencyBonus;
            }
        }
        return 2;
    }

}
