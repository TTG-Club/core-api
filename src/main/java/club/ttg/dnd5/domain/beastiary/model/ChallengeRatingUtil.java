package club.ttg.dnd5.domain.beastiary.model;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ChallengeRatingUtil {
    public String getChallengeRating(long exp) {
        String cr = "0";
        if ((exp > 10) && (exp <= 25)) {
            cr = "1/8";
        } else if ((exp > 25) && (exp <= 50)) {
            cr = "1/8";
        } else if ((exp > 50) && (exp <= 100)) {
            cr = "1/4";
        } else if ((exp > 100) && (exp <= 200)) {
            cr = "1/2";
        } else if ((exp > 200) && (exp <= 450)) {
            cr = "1";
        } else if ((exp > 450) && (exp <= 700)) {
            cr = "2";
        } else if ((exp > 700) && (exp <= 1100)) {
            cr = "3";
        } else if ((exp > 1100) && (exp <= 1800)) {
            cr = "4";
        } else if ((exp > 1800) && (exp <= 2300)) {
            cr = "5";
        } else if ((exp > 2300) && (exp <= 2900)) {
            cr = "6";
        } else if ((exp > 2900) && (exp <= 3900)) {
            cr = "7";
        } else if ((exp > 3900) && (exp <= 5000)) {
            cr = "8";
        } else if ((exp > 5000) && (exp <= 5900)) {
            cr = "9";
        } else if ((exp > 5900) && (exp <= 7200)) {
            cr = "10";
        } else if ((exp > 7200) && (exp <= 8400)) {
            cr = "11";
        } else if ((exp > 8400) && (exp <= 10000)) {
            cr = "12";
        } else if ((exp > 10000) && (exp <= 11500)) {
            cr = "13";
        } else if ((exp > 11500) && (exp <= 13000)) {
            cr = "14";
        } else if ((exp > 13000) && (exp <= 15000)) {
            cr = "15";
        } else if ((exp > 15000) && (exp <= 18000)) {
            cr = "16";
        } else if ((exp > 18000) && (exp <= 20000)) {
            cr = "17";
        } else if ((exp > 20000) && (exp <= 22000)) {
            cr = "18";
        } else if ((exp > 22000) && (exp <= 25000)) {
            cr = "19";
        } else if ((exp > 25000) && (exp <= 33000)) {
            cr = "20";
        } else if ((exp > 33000) && (exp <= 41000)) {
            cr = "21";
        } else if ((exp > 41000) && (exp <= 50000)) {
            cr = "22";
        } else if ((exp > 50000) && (exp <= 62000)) {
            cr = "23";
        } else if ((exp > 62000) && (exp <= 75000)) {
            cr = "24";
        } else if ((exp > 75000) && (exp <= 90000)) {
            cr = "25";
        } else if ((exp > 90000) && (exp <= 105000)) {
            cr = "26";
        } else if ((exp > 105000) && (exp <= 120000)) {
            cr = "27";
        } else if ((exp > 120000) && (exp <= 135000)) {
            cr = "28";
        } else if ((exp > 135000) && (exp <= 155000)) {
            cr = "29";
        } else if (exp > 155000) {
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
