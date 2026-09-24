package club.ttg.dnd5.domain.bastion.rest.dto;

import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilityChoice;
import club.ttg.dnd5.domain.bastion.model.FacilityEnlargement;
import club.ttg.dnd5.domain.bastion.model.FacilityFeature;
import club.ttg.dnd5.domain.bastion.model.FacilityOrderOption;
import club.ttg.dnd5.domain.bastion.model.FacilityPrerequisite;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Форма сооружения бастиона — то, что шлёт редактор и отдаёт {@code GET /bastions/{url}/raw}.
 *
 * <p>Структурированные блоки идут доменной моделью как есть, как у черт: редактор и
 * мини-игра читают одну и ту же форму. Сохранение перезаписывает блоки целиком.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class BastionFacilityRequest extends BaseRequest {
    @Schema(description = "Вид сооружения", examples = {"BASIC", "SPECIAL"})
    private FacilityCategory category;

    @Schema(description = "Требуемый уровень персонажа; у базовых сооружений пуст", examples = {"5", "9", "13", "17"})
    private Long level;

    @Schema(description = "Требование помимо уровня; null — нет")
    private FacilityPrerequisite prerequisite;

    @Schema(description = "Пространство")
    private FacilitySpace space;

    @Schema(description = "Число наёмников")
    private Integer hirelings;

    @Schema(description = "Можно ли иметь больше одного такого сооружения")
    private Boolean repeatable;

    @Schema(description = "Приказы сооружения")
    private List<BastionOrder> orders;

    @Schema(description = "Варианты исполнения приказов")
    private List<FacilityOrderOption> orderOptions;

    @Schema(description = "Расширение сооружения; null — не расширяется")
    private FacilityEnlargement enlargement;

    @Schema(description = "Постоянные свойства сооружения")
    private List<FacilityFeature> features;

    @Schema(description = "Выборы игрока для сооружения")
    private List<FacilityChoice> choices;
}
