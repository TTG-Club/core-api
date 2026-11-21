package club.ttg.dnd5.domain.feat.repository;

/**
 * Лёгкая проекция черты: только url и название, без гидрации jsonb-полей.
 * Используется, чтобы подставить актуальные названия черт в ссылки предыстории.
 */
public interface FeatNameRef {
    String getUrl();

    String getName();
}
