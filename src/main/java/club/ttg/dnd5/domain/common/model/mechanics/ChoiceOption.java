package club.ttg.dnd5.domain.common.model.mechanics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Допустимое значение выбора.
 *
 * <p>{@link #value} трактуется по {@link ChoiceType}: для словарных типов это имя enum'а
 * ({@code STEALTH}, {@code FIRE}, {@code CHARISMA}), для ссылочных — url сущности
 * ({@code wizard-phb}, {@code thieves-tools-phb}), для {@link ChoiceType#OPTION} — любой
 * стабильный ключ.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChoiceOption {
    @Schema(description = "Значение: имя enum'а, url сущности или ключ варианта", example = "PERCEPTION")
    private String value;

    @Schema(description = "Подпись для игрока", example = "Внимательность")
    private String name;
}
