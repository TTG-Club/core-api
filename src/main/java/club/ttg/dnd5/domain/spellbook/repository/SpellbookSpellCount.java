package club.ttg.dnd5.domain.spellbook.repository;

import java.util.UUID;

/**
 * Счётчики заклинаний книги для списка книг: одним запросом на все книги пользователя,
 * без загрузки самих заклинаний.
 */
public interface SpellbookSpellCount {

    UUID getSpellbookId();

    long getTotal();

    long getPrepared();
}
