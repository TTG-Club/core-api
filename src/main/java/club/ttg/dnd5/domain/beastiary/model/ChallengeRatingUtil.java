package club.ttg.dnd5.domain.beastiary.model;

import lombok.experimental.UtilityClass;

@Deprecated
@UtilityClass
public class ChallengeRatingUtil {
    public String getChallengeRating(long exp) {
        String cr = "0";
        if (exp == 10 || exp == 0) {
            cr = "0";
        } else if (exp == 25) {
            cr = "1/8";
        } else if (exp == 50) {
            cr = "1/4";
        } else if (exp == 100) {
            cr = "1/2";
        } else if (exp == 200) {
            cr = "1";
        } else if (exp == 450) {
            cr = "2";
        } else if (exp == 700) {
            cr = "3";
        } else if (exp == 1_100) {
            cr = "4";
        } else if (exp == 1_800) {
            cr = "5";
        } else if (exp == 2_300) {
            cr = "6";
        } else if (exp == 2_900) {
            cr = "7";
        } else if (exp == 3_900) {
            cr = "8";
        } else if (exp == 5_000) {
            cr = "9";
        } else if (exp == 5_900) {
            cr = "10";
        } else if (exp == 7_200) {
            cr = "11";
        } else if (exp == 8_400) {
            cr = "12";
        } else if (exp == 10_000) {
            cr = "13";
        } else if (exp == 11_500) {
            cr = "14";
        } else if (exp == 13_000) {
            cr = "15";
        } else if (exp == 15_000) {
            cr = "16";
        } else if (exp == 18_000) {
            cr = "17";
        } else if (exp == 20_000) {
            cr = "18";
        } else if (exp == 22_000) {
            cr = "19";
        } else if (exp == 25_000) {
            cr = "20";
        } else if (exp == 33_000) {
            cr = "21";
        } else if (exp == 41_000) {
            cr = "22";
        } else if (exp == 50_000) {
            cr = "23";
        } else if (exp == 62_000) {
            cr = "24";
        } else if (exp == 75_000) {
            cr = "25";
        } else if (exp == 90_000) {
            cr = "26";
        } else if (exp == 105_000) {
            cr = "27";
        } else if (exp == 120_000) {
            cr = "28";
        } else if (exp == 135_000) {
            cr = "29";
        } else if (exp == 155_000) {
            cr = "30";
        }
        return cr;
    }

    /**
     * CR 0-4   → БМ=2
     * CR 5-8   → БМ=3
     * CR 9-12  → БМ=4
     * CR 13-16 → БМ=5
     * CR 17-20 → БМ=6
     * CR 21-24 → БМ=7
     * CR 25-28 → БМ=8
     * CR 29-30 → БМ=9
     */
    public String getProficiencyBonus(String CR) {
        return switch (CR) {
            case "5", "6", "7", "8" -> "3";
            case "9", "10", "11", "12" -> "4";
            case "13", "14", "15", "16" -> "5";
            case "17", "18", "19", "20" -> "6";
            case "21", "22", "23", "24" -> "7";
            case "25", "26", "27", "28" -> "8";
            case "29", "30" -> "9";
            default -> "2";
        };
    }
}
