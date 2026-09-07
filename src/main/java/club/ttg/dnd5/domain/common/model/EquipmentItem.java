package club.ttg.dnd5.domain.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Предмет в варианте стартового снаряжения.
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class EquipmentItem {
    @Schema(description = "URL предмета", example = "dagger")
    private String url;

    @Schema(description = "Название предмета на момент сохранения", example = "Кинжал")
    private String name;

    @Schema(description = "Количество предметов", example = "2")
    private Integer quantity;

    @Schema(description = "Уточнение к предмету", example = "по вашему выбору")
    private String description;

    /**
     * Раздел карточки: предметы и магические предметы лежат в разных справочниках,
     * а слаг у них общего вида. Без раздела ни ссылку на карточку не построить, ни
     * название не обновить — искать пришлось бы в обоих справочниках наугад.
     *
     * <p>У стартового снаряжения предысторий и классов не заполнен: там выбирают
     * только обычные предметы.</p>
     */
    @Schema(description = "Раздел карточки предмета", example = "items")
    private SectionType section;

    /**
     * Позиция без раздела — стартовое снаряжение предыстории и класса.
     *
     * @param url слаг карточки предмета.
     * @param name название на момент сохранения.
     * @param quantity количество.
     * @param description уточнение к позиции.
     */
    public EquipmentItem(String url, String name, Integer quantity, String description) {
        this(url, name, quantity, description, null);
    }
}
