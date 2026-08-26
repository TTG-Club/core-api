package club.ttg.dnd5.domain.background.rest.dto;

import club.ttg.dnd5.domain.background.model.BackgroundToolChoice;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.common.model.EquipmentOption;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import club.ttg.dnd5.dto.base.serializer.FormattedMarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Schema(description = "Предыстория запрос")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BackgroundRequest extends BaseRequest {
    @Schema(description = "Характеристики:", examples = {"STRENGTH", "DEXTERITY"})
    private Set<Ability> abilityScores;
    @Schema(description = "URL черты")
    private String featUrl;

    /**
     * Черты на выбор, когда предыстория не называет одну. Рядом с {@link #featUrl}, а не
     * вместо него: у предысторий книги черта одна, и она же участвует в фильтрах.
     */
    @Schema(description = "Черты на выбор")
    private List<EntityRef> featChoices;

    @Schema(description = "Суффикс для черты (например просвещенный в магию)")
    private String featSuffix;
    @Schema(description = "Навыки", examples = {"ACROBATICS", "ATHLETICS"})
    private Set<Skill> skillsProficiencies;
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @JsonSerialize(using = FormattedMarkupDescriptionSerializer.class)
    @Schema(description = "Владение инструментами")
    private String toolProficiency;

    /**
     * Владение инструментами ссылками на записи раздела «Предметы» — главнее свободного
     * текста {@link #toolProficiency}, который остаётся у непереведённых записей.
     */
    @Schema(description = "Владение инструментами ссылками на предметы")
    private List<EntityRef> toolProficiencies;

    @Schema(description = "Владение инструментами на выбор игрока")
    private BackgroundToolChoice toolChoice;

    /**
     * Расширенные дары предыстории — моделью черты, как они лежат в записи
     * ({@code Background.mechanics}).
     *
     * <p>Как и у черты, перезаписывается ЦЕЛИКОМ: блок, которого мастерская не знает,
     * стирается при первом же сохранении.</p>
     */
    @Schema(description = "Расширенные дары предыстории")
    private FeatMechanics mechanics;

    /**
     * Активные эффекты предыстории для экспорта в VTTG — рядом с дарами, а не внутри них:
     * дары лист проставляет сам, а эффект меняет числа готовой формулой.
     */
    @Schema(description = "Активные эффекты предыстории для экспорта в VTTG")
    @Nullable
    @Valid
    private List<ActiveEffect> activeEffects;

    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @JsonSerialize(using = FormattedMarkupDescriptionSerializer.class)
    @Schema(description = "Снаряжение")
    private String equipment;
    @Schema(description = "Стартовое снаряжение вариантами выбора: предметы с количеством и монеты")
    private List<EquipmentOption> startingEquipment;
}
