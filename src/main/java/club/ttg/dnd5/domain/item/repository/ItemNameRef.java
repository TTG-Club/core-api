package club.ttg.dnd5.domain.item.repository;

/**
 * Лёгкая проекция предмета: только url и название, без гидрации jsonb-полей.
 * Используется, чтобы подставить актуальные названия предметов в стартовое снаряжение класса.
 */
public interface ItemNameRef {
    String getUrl();

    String getName();
}
