package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Одно изменение сущности для синхронизации с компендиумом VTTG.
 *
 * @param type      тип сущности (значение {@link club.ttg.dnd5.domain.common.model.SectionType}, например "spells")
 * @param url       стабильный идентификатор сущности (естественный ключ)
 * @param updatedAt время изменения на стороне источника (coalesce(updatedAt, createdAt))
 * @param data      полезная нагрузка в формате VTTG ({@code VttgSpell}/{@code VttgCreature})
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VttgChange(String type, String url, Instant updatedAt, Object data) {
}
