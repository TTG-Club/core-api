package club.ttg.dnd5.domain.initiative.rest.dto;

import club.ttg.dnd5.domain.initiative.model.InitiativeParticipantType;
import club.ttg.dnd5.domain.initiative.model.InitiativeRelationType;
import club.ttg.dnd5.domain.initiative.model.InitiativeRollMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Запрос на добавление или обновление участника трекера инициативы")
@Getter
@Setter
public class InitiativeParticipantRequest {
    @Schema(description = "Тип участника: персонаж игрока или существо")
    private InitiativeParticipantType type;

    @Schema(description = "Отношение участника к группе: союзник, враг или нейтральный")
    private InitiativeRelationType relationType;

    @Schema(description = "Имя персонажа или отображаемое имя участника", examples = "Аэлар")
    private String name;

    @Schema(description = "Уровень персонажа игрока, используется для расчета сложности боевого столкновения", examples = "5")
    private Integer level;

    @Schema(description = "URL существа из бестиария для участника типа CREATURE", examples = "goblin")
    private String sourceCreatureId;

    @Schema(description = "Количество одинаковых существ, которые нужно добавить одним запросом", examples = "3")
    private Integer count = 1;

    @Schema(description = "Максимальные хиты участника", examples = "38")
    private Integer hpMax;

    @Schema(description = "Текущие хиты участника", examples = "38")
    private Integer hpCurrent;

    @Schema(description = "Временные хиты участника", examples = "8")
    private Integer hpTemporary;

    @Schema(description = "Бонус к инициативе", examples = "3")
    private Integer initiativeBonus;

    @Schema(description = "Бонус Ловкости для разрешения ничьих по инициативе", examples = "2")
    private Integer dexterityBonus;

    @Schema(description = "Режим броска инициативы: ручной, обычный, с преимуществом или с помехой")
    private InitiativeRollMode rollMode;

    @Schema(description = "Ручное значение броска d20 для инициативы", examples = "14")
    private Integer rollValue;
}
