package club.ttg.dnd5.domain.background.rest.dto;

import club.ttg.dnd5.domain.background.model.BackgroundToolChoice;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.domain.common.rest.dto.EquipmentOptionDto;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.feat.rest.dto.FeatGrantedSpellResponse;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Collection;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Schema(description = "Информация о предыстории")
@Getter
@Setter
public class BackgroundDetailResponse extends BaseResponse {
    @Schema(description = "Характеристики:")
    private String abilityScores;
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    @Schema(description = "Название черты")
    private String feat;

    /**
     * Черты на выбор, когда предыстория не называет одну. Ссылками: страница и лист
     * персонажа сами достают по ним карточку черты, как по любой другой ссылке разметки.
     */
    @Schema(description = "Черты на выбор")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<EntityRef> featChoices;

    @Schema(description = "Навыки")
    private String skillProficiencies;
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    @Schema(description = "Владение инструментами")
    private String toolProficiency;

    /**
     * Владение инструментами ссылками. Едет рядом с текстом {@link #toolProficiency}, а не
     * вместо него: текст читает страница, а ссылки — лист персонажа, которому нужен адрес
     * карточки, чтобы выдать владение и показать описание инструмента.
     */
    @Schema(description = "Владение инструментами ссылками на предметы")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<EntityRef> toolProficiencies;

    @Schema(description = "Владение инструментами на выбор игрока")
    private BackgroundToolChoice toolChoice;

    /**
     * Расширенные дары предыстории — доменной моделью как есть, ровно как их принимает
     * {@link BackgroundRequest#getMechanics()}. Почему моделью черты — см.
     * {@code Background.mechanics}.
     */
    @Schema(description = "Расширенные дары предыстории")
    private FeatMechanics mechanics;

    /**
     * Активные эффекты предыстории в вокабуляре VTTG. Отдаются вместе с деталью, а не
     * только в «сыром» ответе мастерской: их считает лист персонажа сайта — так же, как
     * эффекты черты и предмета. Пустой список в ответ не пишется.
     */
    @Schema(description = "Активные эффекты предыстории")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ActiveEffect> activeEffects;

    /**
     * Заклинания из {@code mechanics.spells}, дополненные данными справочника.
     *
     * <p>Той же формой, что у черты ({@code FeatDetailResponse.grantedSpells}): дары у
     * предыстории лежат в той же модели, и второй формы для того же смысла заводить не за
     * чем.</p>
     */
    @Schema(description = "Выдаваемые предысторией заклинания с данными справочника")
    private Collection<FeatGrantedSpellResponse> grantedSpells;

    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    @Schema(description = "Снаряжение")
    private String equipment;
    @Schema(description = "Стартовое снаряжение вариантами выбора")
    private List<EquipmentOptionDto> startingEquipment;
}
