package club.ttg.dnd5.domain.bastion.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Справочники и общие числа правил бастиона — для сайта, редактора и мини-игры. */
@Schema(description = "Правила бастиона")
public record BastionRulesResponse(
        @Schema(description = "Виды сооружений") List<BastionLabel> categories,
        @Schema(description = "Пространства с ценами для базовых сооружений") List<SpaceRule> spaces,
        @Schema(description = "Приказы бастиона") List<OrderRule> orders,
        @Schema(description = "Требования специализированных сооружений") List<BastionLabel> prerequisites,
        @Schema(description = "Число специализированных сооружений по уровню персонажа") List<ProgressionRule> specialFacilities,
        @Schema(description = "Пространства базовых сооружений, с которыми появляется бастион") List<String> startingBasicFacilities,
        @Schema(description = "Защитные стены") DefensiveWallRule defensiveWall) {

    @Schema(description = "Пространство")
    public record SpaceRule(
            @Schema(description = "Код") String value,
            @Schema(description = "Подпись") String name,
            @Schema(description = "Максимальная площадь, квадратов") int squares,
            @Schema(description = "Стоимость постройки базового сооружения, зм") int buildCost,
            @Schema(description = "Время постройки базового сооружения, дни") int buildDays,
            @Schema(description = "Код пространства после расширения; null — расширять некуда") String enlargeTo,
            @Schema(description = "Стоимость расширения базового сооружения, зм") Integer enlargeCost,
            @Schema(description = "Время расширения базового сооружения, дни") Integer enlargeDays) {
    }

    @Schema(description = "Приказ")
    public record OrderRule(
            @Schema(description = "Код") String value,
            @Schema(description = "Подпись") String name,
            @Schema(description = "Английское название") String english,
            @Schema(description = "Описание") String description,
            @Schema(description = "Отдаётся всему бастиону, а не сооружению") boolean bastionWide) {
    }

    @Schema(description = "Прогрессия специализированных сооружений")
    public record ProgressionRule(
            @Schema(description = "Уровень персонажа") int level,
            @Schema(description = "Всего специализированных сооружений") int count) {
    }

    @Schema(description = "Защитная стена")
    public record DefensiveWallRule(
            @Schema(description = "Высота, футы") int height,
            @Schema(description = "Стоимость 5-футового квадрата, зм") int costPerSquare,
            @Schema(description = "Время постройки 5-футового квадрата, дни") int daysPerSquare,
            @Schema(description = "Уменьшение числа кубиков потерь защитников при атаке") int defenderDiceReduction) {
    }
}
