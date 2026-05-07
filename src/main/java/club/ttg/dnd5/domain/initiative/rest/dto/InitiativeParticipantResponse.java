package club.ttg.dnd5.domain.initiative.rest.dto;

import club.ttg.dnd5.domain.initiative.model.InitiativeParticipantState;
import club.ttg.dnd5.domain.initiative.model.InitiativeParticipantType;
import club.ttg.dnd5.domain.initiative.model.InitiativeRelationType;
import club.ttg.dnd5.domain.initiative.model.InitiativeRollMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Участник трекера инициативы")
@Getter
@Builder
public class InitiativeParticipantResponse {
    @Schema(description = "Идентификатор участника")
    private UUID id;

    @Schema(description = "Тип участника: персонаж игрока или существо")
    private InitiativeParticipantType type;

    @Schema(description = "Отношение участника к группе: союзник, враг или нейтральный")
    private InitiativeRelationType relationType;

    @Schema(description = "Имя участника")
    private String name;

    @Schema(description = "Базовое имя существа или персонажа без нумерации")
    private String baseName;

    @Schema(description = "Отображаемое имя участника в трекере")
    private String displayName;

    @Schema(description = "Уровень персонажа игрока")
    private Integer level;

    @Schema(description = "URL исходного существа из бестиария")
    private String sourceCreatureId;

    @Schema(description = "Порядковый номер среди одинаковых существ")
    private Integer sameCreatureIndex;

    @Schema(description = "Максимальные хиты")
    private int hpMax;

    @Schema(description = "Текущие хиты")
    private int hpCurrent;

    @Schema(description = "Временные хиты")
    private int hpTemporary;

    @Schema(description = "Состояние участника: активен, без сознания или мертв")
    private InitiativeParticipantState state;

    @Schema(description = "Бонус к инициативе")
    private int initiativeBonus;

    @Schema(description = "Бонус Ловкости для разрешения ничьих")
    private int dexterityBonus;

    @Schema(description = "Режим броска инициативы")
    private InitiativeRollMode rollMode;

    @Schema(description = "Все значения d20, выпавшие при броске инициативы")
    private List<Integer> rolls;

    @Schema(description = "Выбранное значение d20 после учета преимущества или помехи")
    private Integer rollValue;

    @Schema(description = "Итог инициативы: выбранное значение d20 плюс бонус инициативы")
    private Integer initiativeTotal;

    @Schema(description = "Порядок участника в списке инициативы")
    private int orderIndex;

    @Schema(description = "Раунд, в котором участник был добавлен")
    private int addedRound;

    @Schema(description = "Дата создания участника")
    private Instant createdAt;

    @Schema(description = "Дата последнего обновления участника")
    private Instant updatedAt;
}
