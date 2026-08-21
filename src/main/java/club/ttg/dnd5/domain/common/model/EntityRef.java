package club.ttg.dnd5.domain.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ссылка на другую сущность справочника из JSONB-поля.
 *
 * <p>Кроме {@code url} хранит снимок названия на момент сохранения — по тем же причинам,
 * что и {@link EquipmentItem}: список читается без join'а, а переименование сущности не
 * оставляет в форме пустую строку.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityRef {
    @Schema(description = "URL сущности", example = "mark-of-healing-efa")
    private String url;

    @Schema(description = "Название на момент сохранения", example = "Метка исцеления")
    private String name;
}
