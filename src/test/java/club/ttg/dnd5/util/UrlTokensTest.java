package club.ttg.dnd5.util;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class UrlTokensTest {
    private static final Map<String, String> RENAMES = Map.of(
            "fireball-phb", "fireball-ph",
            "light-phb", "light-ph");
    private static final Pattern PATTERN = UrlTokens.pattern(RENAMES.keySet());

    /** Адрес меняется в разметке, JSON и путях сайта. */
    @Test
    void replacesWholeUrls() {
        assertEquals("{@spell Огненный шар|url:fireball-ph}",
                UrlTokens.replace("{@spell Огненный шар|url:fireball-phb}", RENAMES, PATTERN));
        assertEquals("{\"url\":\"fireball-ph\",\"name\":\"Свет\",\"ref\":\"light-ph\"}",
                UrlTokens.replace("{\"url\":\"fireball-phb\",\"name\":\"Свет\",\"ref\":\"light-phb\"}", RENAMES, PATTERN));
        assertEquals("[свет](/spells/light-ph)",
                UrlTokens.replace("[свет](/spells/light-phb)", RENAMES, PATTERN));
        assertEquals("fireball-ph", UrlTokens.replace("fireball-phb", RENAMES, PATTERN));
    }

    /** Адрес внутри другого адреса не трогается. */
    @Test
    void keepsLongerUrls() {
        String text = "greater-fireball-phb fireball-phb-2 fireball-phb24 xfireball-phb";

        assertSame(text, UrlTokens.replace(text, RENAMES, PATTERN));
    }
}
