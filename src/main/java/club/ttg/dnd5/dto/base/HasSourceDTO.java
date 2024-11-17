package club.ttg.dnd5.dto.base;


/**
 * Интерфейс `HasSourceDTO` определяет контракт для объектов, которые ссылаются на источник
 * (например, книгу правил или справочные материалы) и конкретную страницу в этом источнике.
 *
 * В данном контексте термин "source" обычно представлен акронимом, например:
 * - "PHB" (Player's Handbook — Книга игрока)
 * - "DMG" (Dungeon Master's Guide — Руководство мастера подземелий)
 * и т.д.
 */
public interface HasSourceDTO {
    String getSource();
    Short getPage();
    void setPage(Short page);
    void setSource(String source);
}
