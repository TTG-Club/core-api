package club.ttg.dnd5.domain.species.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonRootName(value = "properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class SpeciesPropertiesResponse {
    @JsonProperty(value = "speed")
    private String speed;
    @Schema(description = "Размеры")
    private String size;

    /**
     * Те же размеры и скорость структурой, как их хранит запись. Строки выше — для
     * показа человеку, эти поля — для потребителей, которые свойства ПРИМЕНЯЮТ (лист
     * персонажа): им иначе пришлось бы разбирать русский текст обратно.
     */
    @Schema(description = "Размеры записи структурой")
    private List<SpeciesSizeDto> sizes;

    @Schema(description = "Скорости записи числами")
    private MovementAttributes movement;
    @Schema(description = "Тип существа")
    private String type;
    /**
     * Числом, а не строкой как скорость и размер: лист складывает его с чужими чувствами.
     *
     * <p>Вычисляется сервисом как наибольшая дальность чувства {@code DARKVISION} в механике
     * записи и её умений (у происхождения — с учётом родителя): своего поля у записи нет,
     * тёмное зрение дарит умение. Статблоку и листу персонажа перенос не виден.</p>
     */
    @Schema(description = "Дальность тёмного зрения в футах", example = "60")
    private Integer darkVision;
    /** Обычное зрение в футах — поле записи, как есть; {@code null} — не задано. */
    @Schema(description = "Дальность обычного зрения в футах", example = "60")
    private Integer vision;
}