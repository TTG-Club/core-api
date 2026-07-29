package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.common.dictionary.Coin;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Вариант стартового снаряжения класса — подблок «А», «Б» и т.д.
 * Состоит из списка предметов с количеством и суммы монет.
 * Метка варианта не хранится: она выводится из порядка при отдаче ответа.
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class ClassEquipmentOption {
    @Schema(description = "Предметы варианта")
    private List<ClassEquipmentItem> items;

    @Schema(description = "Количество монет", example = "15")
    private Integer coins;

    @Schema(description = "Тип монет")
    private Coin coin = Coin.GC;
}
