package club.ttg.dnd5.domain.feat.model.mechanics;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.model.EntityRef;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Заклинания, которые черта даёт знать без выбора: «Отмеченный драконом» приносит свой
 * набор, «Дар совершенного полёта» — «Полёт».
 *
 * <p>Выбор заклинания сюда не идёт и остаётся в {@code mechanics.choices}: у него есть
 * количество и фильтр ({@link SpellFilter} — круг, школа, время накладывания), а
 * «Посвящённый в магию» и вовсе сперва просит выбрать список класса. Здесь ни того, ни
 * другого не нужно — достаточно перечислить выданное.</p>
 *
 * <p>Ссылками на справочник, а не словарём: заклинания — сущности раздела, и лист хранит
 * вместе с названием ссылку, чтобы открыть описание прямо из книги заклинаний. Круг и
 * школа берутся из самой записи справочника и здесь не дублируются — иначе они разошлись
 * бы с каталогом при правке заклинания.</p>
 *
 * <p>Ограничений использования («один раз до продолжительного отдыха») здесь нет: это
 * счётчик, а не свойство заклинания. Ресурсы черт лист пока не описывает вовсе, и они
 * остаются в описании — как условные эффекты в {@link FeatModifiers}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpellGrant {
    /** Заклинания и заговоры, которые черта даёт знать. */
    @Schema(description = "Заклинания из справочника")
    private List<EntityRef> spells;

    /**
     * Заклинательная характеристика для этих заклинаний. {@code null} — черта её не
     * задаёт: либо характеристику выбирает игрок (тип выбора
     * {@link ChoiceType#SPELLCASTING_ABILITY}), либо она берётся от класса.
     */
    @Schema(description = "Заклинательная характеристика", examples = {"INTELLIGENCE", "CHARISMA"})
    private Ability spellcastingAbility;

    /**
     * Заклинания не нужно готовить — они всегда доступны. Так устроены врождённые
     * заклинания вида, и лист уже умеет их показывать без отметки подготовки.
     *
     * <p>{@code null} читается как «готовить нужно»: заклинание ложится в книгу наравне
     * с остальными.</p>
     */
    @Schema(description = "Заклинания всегда подготовлены")
    private Boolean alwaysPrepared;
}
