package club.ttg.dnd5.domain.common.model.mechanics;

import club.ttg.dnd5.domain.common.dictionary.Ability;
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

    /**
     * Характеристика, от которой считаются ЭТИ заклинания. {@code null} — группа её не
     * задаёт, и характеристику берут выше: сперва ответ игрока, затем
     * {@link SpellGrant#getSpellcastingAbility()}, затем класс, чья это магия.
     *
     * <p>У группы, а не только у записи целиком: один набор заклинаний записи может
     * считаться от одной характеристики, другой — от другой, и одно поле на всех
     * заставило бы заводить ради этого вторую запись.</p>
     */
    @Schema(description = "Характеристика заклинаний группы", examples = {"WISDOM", "CHARISMA"})
    private Ability spellcastingAbility;

    /**
     * ЭТИ заклинания не нужно готовить. {@code null} — группа отметку не задаёт, и её
     * берут у записи ({@link SpellGrant#getAlwaysPrepared()}).
     *
     * <p>У группы по той же причине, что и характеристику: заклинания домена всегда
     * подготовлены, а заклинание, выданное тем же умением сверх них, подготовку
     * занимает.</p>
     */
    @Schema(description = "Заклинания группы всегда подготовлены")
    private Boolean alwaysPrepared;

    public GrantedSpellRef(String url, String name, Integer requiredLevel) {
        super(url, name);
        this.requiredLevel = requiredLevel;
    }
}
