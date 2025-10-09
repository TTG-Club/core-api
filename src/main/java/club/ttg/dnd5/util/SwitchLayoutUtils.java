package club.ttg.dnd5.util;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class SwitchLayoutUtils {
    private static final Map<Character, Character> LAYOUT_SWITCH_MAP;

    static {
        LAYOUT_SWITCH_MAP = new HashMap<>();
        String ru = "йцукенгшщзфывапролдячсмитьбю";
        String en = "qwertyuiopasdfghjklzxcvbnm,.";

        for (int i = 0; i < ru.length(); i++) {
            LAYOUT_SWITCH_MAP.put(ru.charAt(i), en.charAt(i));
            LAYOUT_SWITCH_MAP.put(Character.toUpperCase(ru.charAt(i)), Character.toUpperCase(en.charAt(i)));
            LAYOUT_SWITCH_MAP.put(en.charAt(i), ru.charAt(i));
            LAYOUT_SWITCH_MAP.put(Character.toUpperCase(en.charAt(i)), Character.toUpperCase(ru.charAt(i)));
        }
    }

    public static String switchLayout(String line) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            result.append(LAYOUT_SWITCH_MAP.getOrDefault(c, c));
        }
        return result.toString();
    }
}
