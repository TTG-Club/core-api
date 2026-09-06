package club.ttg.dnd5.domain.beastiary.model.action;

import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.DamagePart;
import club.ttg.dnd5.domain.spell.model.AreaOfEffect;
import club.ttg.dnd5.domain.spell.model.enums.SpellSaveEffect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

/**
 * Боевая механика записи существа: действия, реакции, умения, эффекта логова.
 *
 * <p>Тот же по смыслу объект, что {@code SpellEffect} у заклинания, и зеркало
 * {@code CreatureAction} игровой системы. До него механику приходилось выуживать
 * регулярками из текста описания — здесь она задана числами.</p>
 *
 * <p>Формулы существа плоские: модификатор уже вшит в число («1к8 + 3»), токены
 * {@code @mod.*}, {@code @prof}, {@code @level} и {@code @classLevel} системе
 * запрещены — у существа нет ни характеристик листа, ни уровня в классе.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatureActionEffect {
    /** Рукопашная, дальнобойная или обе — как в справочнике сайта. */
    private AttackType attackType;

    /** Бонус к попаданию плоским числом: у существа он не выводится. */
    private Integer attackBonus;

    /** Досягаемость рукопашной атаки в футах. */
    private Integer reach;

    /**
     * Обычная дальность броска в футах. Плоскими полями, а не объектом
     * {@code range}: {@code long} — ключевое слово Java, а разъезжаться с
     * формой из-за одного имени не стоит.
     */
    private Integer rangeNormal;

    /** Максимальная дальность броска в футах (с помехой). */
    private Integer rangeLong;

    /** Части урона и лечения — общая с заклинанием и эффектом система формул. */
    private List<DamagePart> damageParts;

    /**
     * Спасброски цели. Список, потому что в данных сайта они лежали списком;
     * система знает только один — в выгрузку идёт первый.
     */
    private Collection<SawingThrow> savingThrows;

    /** Что происходит с уроном при успешном спасброске. */
    private SpellSaveEffect saveEffect;

    /** Область воздействия — та же модель и та же трансляция, что у заклинания. */
    private AreaOfEffect areaOfEffect;

    /**
     * Типы урона старых данных. Формой не показываются и в выгрузку сами по
     * себе не идут: они нужны регулярочному разбору описания как подсказка о
     * типе урона. Возятся сквозь форму, чтобы круг «открыл — сохранил» их не
     * стёр у записей, которые механику ещё не завели.
     */
    private Collection<DamageType> damageTypes;

    /**
     * Эффекты, которые запись накладывает при попадании или активации. По
     * умолчанию летят в цель, а не на само существо.
     */
    private List<ActiveEffect> activeEffects;
}
