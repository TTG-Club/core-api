package club.ttg.dnd5.domain.bastion.player.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Сделанный выбор для сооружения игрока: какие варианты выбора справочника
 * ({@code FacilityChoice}) взяты — тип сада, мастер тренировочной зоны и т.п.
 *
 * @param name    название выбора из справочника («Тип сада»)
 * @param options названия выбранных вариантов («Травяной»)
 */
@Schema(description = "Сделанный выбор сооружения")
public record SelectedChoice(String name, List<String> options) {
}
