package club.ttg.dnd5.domain.common.model.mechanics;

import club.ttg.dnd5.domain.common.model.EntityRef;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ссылка на выдаваемое заклинание вместе с уровнем персонажа, с которого оно доступно.
 *
 * <p>Заклинание черты приходит не всегда сразу: у метки дракона «Лечение ран» есть с
 * первого уровня, а «Малое восстановление» — только с третьего; у «Дара камня Тени»
 * заклинания открываются на 4, 5, 9, 13 и 17 уровнях. Без уровня лист выдал бы весь
 * список сразу, и черта оказалась бы сильнее книжной.</p>
 *
 * <p>Наследованием от {@link EntityRef}, а не обёрткой вокруг него: JSON остаётся плоским
 * ({@code {"url": "...", "requiredLevel": 3}}), и уже сохранённые ссылки без уровня
 * читаются как есть. Обёртка {@code {"spell": {...}, "requiredLevel": 3}} потребовала бы
 * миграции всего сохранённого JSONB и правки редактора там, где он ничего не менял.</p>
 *
 * <p>Имя поля повторяет {@code SpeciesInnateSpellRequest.requiredLevel} — у вида
 * врождённые заклинания устроены так же, и два имени для одного и того же смысла
 * пришлось бы держать в голове и на сайте, и в выгрузке.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrantedSpellRef extends EntityRef {
    /**
     * Уровень персонажа, с которого заклинание доступно.
     *
     * <p>{@code null} — с момента взятия черты. Так читаются и все ссылки, сохранённые до
     * появления поля, поэтому отдельного значения «с первого уровня» не нужно.</p>
     */
    @Schema(description = "Уровень персонажа, с которого доступно заклинание", example = "3")
    private Integer requiredLevel;

    public GrantedSpellRef(String url, String name, Integer requiredLevel) {
        super(url, name);
        this.requiredLevel = requiredLevel;
    }
}
