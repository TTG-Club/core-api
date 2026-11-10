package club.ttg.dnd5.domain.vttg.rest.dto;

import java.time.Instant;

/**
 * Версия формата выгрузки VTTG для админки.
 *
 * @param version   текущая версия (отдаётся клиентам как {@code schemaVersion})
 * @param updatedAt когда версию подняли ({@code null} — начальное значение из миграции)
 * @param updatedBy кто поднял (username)
 * @param rebuild   состояние фоновой пересборки выгрузки
 */
public record VttgCompendiumVersionResponse(int version, Instant updatedAt, String updatedBy,
                                            VttgRebuildStatus rebuild) {
}
