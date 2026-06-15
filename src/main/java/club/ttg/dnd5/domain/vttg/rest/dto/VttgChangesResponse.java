package club.ttg.dnd5.domain.vttg.rest.dto;

import java.time.Instant;
import java.util.List;

/**
 * Дельта изменений сущностей TTG Club для применения в компендиум VTTG.
 *
 * <p>Клиент применяет {@code upserts} (создание/обновление) и после успешного применения
 * сохраняет {@code until} как новый курсор {@code since}. Скрытые сущности (мягкое удаление
 * на стороне источника) в дельту не попадают.</p>
 *
 * @param since   нижняя граница окна (исключительно)
 * @param until   верхняя граница окна (включительно); именно это значение клиент сохраняет как новый курсор
 * @param upserts добавленные/изменённые сущности с полезной нагрузкой
 */
public record VttgChangesResponse(Instant since,
                                  Instant until,
                                  List<VttgChange> upserts) {
}
