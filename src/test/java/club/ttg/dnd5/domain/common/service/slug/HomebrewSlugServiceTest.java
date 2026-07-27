package club.ttg.dnd5.domain.common.service.slug;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomebrewSlugServiceTest {

    private final HomebrewSlugService service = new HomebrewSlugService();

    /** Всегда свободно — берётся базовая форма без числового суффикса. */
    private static final Predicate<String> NONE_EXIST = url -> false;

    @Test
    void generate_buildsNamespacedPath() {
        String url = service.generate("magistrus", "fireball", NONE_EXIST);
        assertEquals("u/magistrus/fireball", url);
    }

    @Test
    void generate_normalizesHandleAndStem() {
        // «сырые» значения: регистр, пробелы, апостроф, юникод-мусор по краям
        String url = service.generate("Magistrus", "  Fire Ball!  ", NONE_EXIST);
        assertEquals("u/magistrus/fire-ball", url);
    }

    @Test
    void generate_appendsNumericFallbackOnCollision() {
        Predicate<String> taken = Set.of(
                "u/magistrus/fireball",
                "u/magistrus/fireball-2")::contains;

        String url = service.generate("magistrus", "fireball", taken);
        assertEquals("u/magistrus/fireball-3", url);
    }

    @Test
    void generate_differentOwnersNeverCollide() {
        String a = service.generate("magistrus", "fireball", NONE_EXIST);
        String b = service.generate("vasya", "fireball", NONE_EXIST);
        assertFalse(a.equals(b));
        assertTrue(a.startsWith("u/magistrus/"));
        assertTrue(b.startsWith("u/vasya/"));
    }

    /** Форма homebrew-url дизъюнктна с официальной ({@code {stem}-{acronym}}). */
    @Test
    void generate_isDisjointFromOfficialShape() {
        // юзер с хендлом, совпадающим с акронимом источника, не порождает официальный url
        String url = service.generate("phb", "fireball", NONE_EXIST);
        assertEquals("u/phb/fireball", url);
        assertFalse(url.equals("fireball-phb"));
        assertTrue(HomebrewSlug.isHomebrew(url));
    }

    @Test
    void generate_rejectsEmptyHandle() {
        assertThrows(IllegalArgumentException.class,
                () -> service.generate("!!!", "fireball", NONE_EXIST));
    }

    @Test
    void generate_rejectsEmptyStem() {
        assertThrows(IllegalArgumentException.class,
                () -> service.generate("magistrus", "   ", NONE_EXIST));
    }

    @Test
    void isHomebrew_detectsShapeOnly() {
        assertTrue(HomebrewSlug.isHomebrew("u/magistrus/fireball"));
        assertFalse(HomebrewSlug.isHomebrew("fireball-phb"));
        assertFalse(HomebrewSlug.isHomebrew("unseen-servant-phb"));
        assertFalse(HomebrewSlug.isHomebrew(null));
    }
}
