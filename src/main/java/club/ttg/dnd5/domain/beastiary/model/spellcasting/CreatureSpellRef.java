package club.ttg.dnd5.domain.beastiary.model.spellcasting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Заклинание порции: ссылка на карточку сайта и оговорки статблока к ней.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class CreatureSpellRef {
    @Schema(description = "Слаг карточки заклинания", example = "hold-person-phb")
    private String url;

    /**
     * Название на момент выбора. Снимок, а не подстановка: бэкенд имя не проставляет и не
     * сверяет — оно нужно форме, когда заклинание из каталога удалили.
     */
    @Schema(description = "Название заклинания на момент выбора", example = "Удержание личности")
    private String name;

    /**
     * Круг, которым существо накладывает заклинание: «Воображаемый убийца (версия 6
     * уровня)». Пусто — заклинание идёт своим кругом.
     */
    @Schema(description = "Круг, которым существо накладывает заклинание", example = "6")
    private Integer castLevel;

    @Schema(description = "Оговорка статблока к заклинанию", example = "только на себя")
    private String note;
}
