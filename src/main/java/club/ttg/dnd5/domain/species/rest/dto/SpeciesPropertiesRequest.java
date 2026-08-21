package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@JsonRootName(value = "properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class SpeciesPropertiesRequest {
    @JsonProperty(value = "speed")
    private MovementAttributes movementAttributes = new MovementAttributes();
    @Schema(description = "Размеры")
    private Collection<SpeciesSizeDto> sizes;
    @Schema(description = "Тип существа")
    private CreatureType type;
    /**
     * Дальность тёмного зрения в футах; {@code null} — вида его не имеет.
     *
     * <p>Отдельным полем, а не чувством в механике умения: тёмное зрение есть у половины
     * видов справочника, лист показывает его в шапке рядом со скоростью, а выгрузка
     * компендиума ждёт его в наградах вида ({@code grants}), а не в тексте умения.
     * Чувства, которые вид даёт умением («слепое зрение 10»), живут в
     * {@code features[].mechanics.modifiers.senses}.</p>
     */
    @Schema(description = "Дальность тёмного зрения в футах", example = "60")
    private Integer darkVision;
}