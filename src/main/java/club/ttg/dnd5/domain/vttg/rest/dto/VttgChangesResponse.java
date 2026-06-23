package club.ttg.dnd5.domain.vttg.rest.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Дельта изменений сущностей TTG Club для применения в компендиум VTTG.
 *
 * <p>Клиент применяет {@code upserts} (создание/обновление) и после успешного применения
 * сохраняет {@code until} как новый курсор {@code since}. Скрытые сущности (мягкое удаление
 * на стороне источника) в дельту не попадают.</p>
 *
 * @param until    верхняя граница окна (включительно); именно это значение клиент сохраняет как новый курсор
 * @param upserts  добавленные/изменённые сущности с полезной нагрузкой
 * @param sections дерево разделов («манифест» отображения), см. {@code VttgCompendiumSections#changesTree()}
 */
public record VttgChangesResponse(Instant until,
                                  List<VttgChange> upserts,
                                  List<Map<String, Object>> sections) {
}
