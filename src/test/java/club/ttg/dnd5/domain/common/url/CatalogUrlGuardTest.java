package club.ttg.dnd5.domain.common.url;

import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.exception.EntityExistException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogUrlGuardTest {
    private final EntityUrlHistoryRegistry history = mock(EntityUrlHistoryRegistry.class);
    private final CatalogUrlGuard.Installer installer = new CatalogUrlGuard.Installer(history);
    private final CatalogUrlGuard guard = new CatalogUrlGuard();

    @AfterEach
    void tearDown() {
        installer.uninstall();
    }

    /** Новая запись не занимает прежний адрес другой записи. */
    @Test
    void oldUrlCannotBeTaken() {
        installer.install();
        when(history.isOldUrl("spells", "fireball-phb")).thenReturn(true);

        assertThrows(EntityExistException.class, () -> guard.beforeInsert(spell("fireball-phb")));
        assertDoesNotThrow(() -> guard.beforeInsert(spell("fireball-ph")));
    }

    /** Без реестра (юнит-тесты, старт) проверки нет. */
    @Test
    void withoutRegistryNothingIsChecked() {
        assertDoesNotThrow(() -> guard.beforeInsert(spell("fireball-phb")));
    }

    private static Spell spell(String url) {
        Spell spell = new Spell();
        spell.setUrl(url);
        return spell;
    }
}
