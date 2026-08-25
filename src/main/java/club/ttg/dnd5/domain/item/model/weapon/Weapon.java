package club.ttg.dnd5.domain.item.model.weapon;

import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.Roll;
import club.ttg.dnd5.domain.item.rest.dto.Range;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * Боевые параметры оружия.
 *
 * <p>Поля, добавленные ради паритета с системой D&amp;D приложения, необязательны:
 * пустое значение означает «считать как раньше», и девять сотен записей справочника
 * заполнять заново не нужно. Значения новых полей — вокабуляр VTTG строками
 * ({@code strength}, {@code always}, {@code half}), как у
 * {@code SpellEffect.deliveryType} и активных эффектов: маппер компендиума отдаёт их
 * без перевода словарей.</p>
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Weapon  {
    /** Категория оружия */
    private WeaponCategory category;

    /**
     * Урон связкой «кости + тип». Урон описывают {@link #damageParts}; эта связка
     * остаётся для листа персонажа сайта и записей, сохранённых до частей-формул, и
     * выводится формой из первой части.
     */
    private Damage damage;

    /**
     * Свойства оружия
     */
    private Set<Property> properties;

    /** Приём */
    private Mastery mastery;

    private Range range;

    /**
     * Универсальный урон в прежней форме — пара к {@link #damage}; в частях урона это
     * {@code damageParts[0].versatileFormula}.
     */
    private Roll versatile;

    /**
     * Требуемый тип снаряда для выстрела (только дальнобойного)
     */
    private AmmunitionType ammo;

    /**
     * Боекомплект: количество выстрелов до перезарядки (свойство {@code MAGAZINE})
     */
    private Integer magazine;

    private String additional;

    /**
     * Урон оружия частями-формулами — источник истины боевой механики, единый с
     * заклинаниями. Пусто — урон берётся из прежней связки {@link #damage}.
     */
    private List<DamagePart> damageParts;

    /**
     * Ключ базового вида оружия в справочнике листа ({@code longsword}). По нему
     * сверяется владение оружием. Пусто — выводится из адреса страницы и английского
     * названия.
     */
    private String baseType;

    /** Досягаемость в футах. Пусто — 5, а со свойством «Досягаемость» 10. */
    private Integer reach;

    /**
     * Характеристика броска атаки в вокабуляре VTTG ({@code strength}…
     * {@code charisma}). Пусто — по правилам вида оружия: рукопашное от Силы,
     * дальнобойное от Ловкости, фехтовальное — от большей из двух.
     */
    private String attackAbility;

    /**
     * Режим учёта бонуса мастерства ({@code auto}, {@code always}, {@code never}).
     * Пусто — {@code auto}.
     */
    private String proficiencyMode;

    /** Фиксированный бонус к броску атаки сверх характеристики. */
    private Integer attackBonus;

    /**
     * Характеристика, чей модификатор идёт в урон ({@code strength}…{@code charisma}
     * либо {@code none} — без прибавки). Пусто — та же, что у атаки.
     */
    private String damageAbility;

    /** Фиксированный бонус к урону — пара к {@link #attackBonus}. */
    private Integer damageBonus;

    /**
     * Характеристика спасброска, который оружие заставляет совершить
     * ({@code strength}…{@code charisma}). Пусто — обычная атака с броском попадания.
     */
    private String saveType;

    /**
     * Что происходит с уроном при успешном спасброске: {@code half}, {@code none},
     * {@code special}.
     */
    private String saveEffect;
}
