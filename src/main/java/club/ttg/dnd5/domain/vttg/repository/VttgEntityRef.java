package club.ttg.dnd5.domain.vttg.repository;

import java.time.Instant;

/**
 * Лёгкая проекция изменённой сущности для VTTG: только идентификатор и время изменения,
 * без гидрации jsonb-полей. Используется, чтобы определить состав окна {@code /changes}
 * и сопоставить его с предрассчитанными payload в {@code vttg_export}.
 */
public interface VttgEntityRef {
    String getUrl();

    Instant getChangedAt();
}
