package club.ttg.dnd5.domain.vttg.rest.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Лёгкий статус наличия изменений — для индикатора в VTTG.
 * Не содержит полезной нагрузки, считается дешёвым {@code count}-запросом.
 *
 * @param since      нижняя граница окна (исключительно)
 * @param until      верхняя граница окна (включительно)
 * @param hasUpdates есть ли изменения в окне
 * @param count      число добавленных/изменённых видимых сущностей в окне
 * @param byType     разбивка числа изменений по типам сущностей
 */
public record VttgChangesStatus(Instant since,
                                Instant until,
                                boolean hasUpdates,
                                long count,
                                Map<String, Long> byType) {
}
