package club.ttg.dnd5.domain.feat.model.prerequisite;

import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.model.EntityRef;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * Предварительное условие черты в разобранном виде — чтобы визард выбора черты мог
 * отфильтровать список, а не показывать игроку строку.
 *
 * <p>Заполненные поля соединяются по «И»; множественность внутри поля — по «ИЛИ»
 * ({@link AbilityRequirement#getAnyOf()}, {@link #classFeatures}). Человекочитаемый
 * текст остаётся в {@code Feat.prerequisite} и показывается как есть: разобрать
 * удаётся не всё, а показывать нужно всегда.</p>
 *
 * <p>{@code null} — у черт, сохранённых до появления поля.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeatPrerequisite {
    @Schema(description = "Минимальный уровень персонажа", example = "4")
    private Integer minCharacterLevel;

    @Schema(description = "Требования к значениям характеристик")
    private List<AbilityRequirement> abilities;

    @Schema(description = "Черты, которые нужно иметь")
    private List<EntityRef> feats;

    @Schema(description = "Требуется любая черта метки дракона", example = "false")
    private Boolean anyDragonmark;

    @Schema(description = "Классовые умения, любого из которых достаточно")
    private Set<ClassFeatureRequirement> classFeatures;

    /** Классы, любого из которых достаточно: черты VTMBBB требуют класс «Сородич». */
    @Schema(description = "Классы, любого из которых достаточно")
    private List<EntityRef> classes;

    @Schema(description = "Виды, любого из которых достаточно")
    private List<EntityRef> species;

    @Schema(description = "Предыстории, любой из которых достаточно")
    private List<EntityRef> backgrounds;

    @Schema(description = "Владение доспехами, которое нужно иметь")
    private Set<ArmorCategory> armorProficiency;

    @Schema(description = "Сеттинг кампании", example = "Эберрон")
    private String campaign;

    /**
     * Условие, которому не нашлось поля: «превращение в лича», «первозданное превращение»
     * и прочее, что зависит от событий в игре. Лист такое не проверяет — только показывает.
     */
    @Schema(description = "Условие, которое лист не проверяет",
            example = "превращение в лича")
    private String custom;
}
