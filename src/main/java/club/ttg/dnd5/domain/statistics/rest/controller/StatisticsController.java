package club.ttg.dnd5.domain.statistics.rest.controller;

import club.ttg.dnd5.domain.statistics.rest.dto.CharacterSheetStatisticsResponse;
import club.ttg.dnd5.domain.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Tag(name = "Статистика", description = "API для статистики")
@RequestMapping("/api/v2/statistics/")
@RestController
public class StatisticsController {
    private final StatisticsService statisticsService;

    @Operation(summary = "Количество материалов")
    @GetMapping("/count-all")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject("""
                                            123
                                            """)
                            )
                    )
            }
    )
    public Long countAllMaterials() {
        return statisticsService.countAllMaterials();
    }

    /**
     * Публичная ручка: суммарные числа по всем пользователям, без привязки к владельцу
     * и без данных самих листов. Читают её карточка админ-дашборда и счётчик персонажей
     * на главной странице сайта, поэтому доступ без авторизации, как у {@code /count-all}.
     */
    @Operation(summary = "Количество листов персонажей: всего (с историей удалённых) и активных")
    @GetMapping("/character-sheets")
    public CharacterSheetStatisticsResponse countCharacterSheets() {
        return statisticsService.countCharacterSheets();
    }
}
