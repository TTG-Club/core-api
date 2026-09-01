package club.ttg.dnd5.domain.vttg.rest.dto;

import club.ttg.dnd5.domain.common.model.ActiveEffect;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Магический предмет в формате компендиума VTTG.
 *
 * <p>Соответствует типу {@code GameItem} из VTTG (packages/shared/src/types) для
 * {@code type === 'equipment'}. Поля включаются даже со значением {@code null}
 * (например {@code maxDexBonus}, как в примере wands.json), поэтому {@code NON_NULL}
 * применяется точечно только к действительно опциональному {@code nameEn}.</p>
 */
@Builder
@Getter
public class VttgMagicItem {
    private String id;
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String nameEn;
    private String description;
    /** {@code GameItemType}: "weapon" для оружия, иначе "equipment". */
    private String type;
    /** Отображаемая метка типа (напр. «Снаряжение»/«Оружие»). */
    private String typeLabel;
    /** Slug листа дерева разделов, куда положить запись (weapons/armor/rings/wands/wondrous). */
    private String section;
    /**
     * Раздел сайта в адресе страницы-источника — всегда {@code magic-items}. По паре
     * {@code srcSection}/{@code srcUrl} VTTG находит запись в компендиуме, когда в описании
     * кликают ссылку. С {@code section} не совпадает: там лист дерева компендиума.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String srcSection;
    /**
     * Слаг страницы-источника на сайте. У всех записей, на которые раскрылся один предмет,
     * он ОДИН И ТОТ ЖЕ — слаг родителя: страница на сайте у них общая.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String srcUrl;
    /**
     * Запись — производная от страницы-источника, а не сама она: раскрытие по базовому
     * предмету («полулаты или латы») или по шаблону «+1/+2/+3». Нужен VTTG, чтобы из группы
     * записей с общим {@code srcUrl} выбрать ту, которую открывать по ссылке. У якорной
     * записи опускается.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean srcVariant;
    private int quantity;
    /** Вес в фунтах (в модели источника отсутствует — по умолчанию 0). */
    private double weight;
    /** Стоимость; строковая форма допустима ("" — не задана). */
    private String cost;
    /** {@code ItemRarity}: none, common, uncommon, rare, very-rare, legendary, artifact. */
    private String rarity;
    private boolean equipped;
    /**
     * {@code EquipmentCategory}: wand, ring, wondrous, light, medium, heavy, shield, trinket, clothing...
     * Опускается для оружия (у него своя категория) и для брони без известного класса.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String equipmentCategory;
    /**
     * Доспешные поля. Имеют смысл только для брони и при отсутствии структурных данных
     * опускаются ({@code NON_NULL}) — как в эталоне SRD-бэкапа для не-брони (жезл и т.п.).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer baseArmorAC;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxDexBonus;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean stealthDisadvantage;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer strengthRequirement;
    /** {@code magicAttunement}: "none", "required" или "optional". */
    private String magicAttunement;
    /** Бонус «+1/+2/+3» оружия/брони; опускается, если бонуса нет. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer magicBonus;
    /**
     * Надбавка только к урону сверх {@code magicBonus} (оружие, бьющее сильнее, чем
     * попадающее). Обычному «+N» не нужна: тот бонус потребитель прибавляет и к атаке,
     * и к урону. Опускается, если надбавки нет.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer damageBonus;
    /** Ключ источника из sources.json: "phb", "dmg", "srd"... */
    private String sourceKey;

    /**
     * Активные эффекты предмета — что он меняет на листе персонажа
     * ({@code MagicItemMechanics.activeEffects}). Передаются без преобразования: модель
     * уже в вокабуляре VTTG, как у заклинаний ({@code VttgSpell.activeEffects}).
     *
     * <p>Потребитель собирает эффекты надетых предметов наравне с эффектами самого
     * персонажа, поэтому плащ защиты поднимает КД и спасброски сам. Различения
     * «при себе / надет / в руке» ({@code MagicItemActivation}) у него нет — эффекты
     * включает пара «надет + настроен», — поэтому условие активации в выгрузку не идёт.</p>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ActiveEffect> activeEffects;

    /**
     * Заряды предмета ({@code MagicItemResource}). Опускается у предметов без зарядов.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Uses uses;

    /**
     * Свойства, которые лист показывает справкой, но не считает: дыхание под водой,
     * иммунитет к чтению мыслей и прочее ({@code MagicItemMechanics.passive}).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String passive;

    /**
     * Боевые/доспешные поля, выведенные из базового предмета (по {@code clarification}):
     * {@code baseType}, {@code damageParts}, {@code weaponCategory}, {@code baseArmorAC} и т.д.
     * Сериализуются как поля верхнего уровня (см. {@code VttgItemMapper}); пусто — не выводятся.
     */
    @Getter(AccessLevel.NONE)
    private Map<String, Object> mechanics;

    @JsonAnyGetter
    public Map<String, Object> getMechanics() {
        return mechanics;
    }

    @Getter(AccessLevel.NONE)
    private boolean isMagical;
    /** Предметом можно пользоваться как заклинательной фокусировкой (посохи, палочки, жезлы). */
    @Getter(AccessLevel.NONE)
    private boolean isFocus;
    /** Адамантиновый предмет — отдельное свойство экипировки в системе. */
    @Getter(AccessLevel.NONE)
    private boolean isAdamantine;
    @Getter(AccessLevel.NONE)
    private boolean isSRD;
    @Getter(AccessLevel.NONE)
    private boolean isReadOnly;

    @JsonProperty("isMagical")
    public boolean isMagical() {
        return isMagical;
    }

    @JsonProperty("isFocus")
    public boolean isFocus() {
        return isFocus;
    }

    @JsonProperty("isAdamantine")
    public boolean isAdamantine() {
        return isAdamantine;
    }

    @JsonProperty("isSRD")
    public boolean isSRD() {
        return isSRD;
    }

    @JsonProperty("isReadOnly")
    public boolean isReadOnly() {
        return isReadOnly;
    }

    /**
     * Заряды предмета в формате компендиума VTTG ({@code GameItem.uses}).
     *
     * <p>{@code current} в справочнике всегда равен {@code max}: остаток — состояние
     * конкретного экземпляра на листе, а не свойство записи каталога. Потребитель
     * заводит предмет полным и списывает заряды сам.</p>
     *
     * @param max      максимум зарядов
     * @param current  остаток; в выгрузке всегда равен {@code max}
     * @param recovery когда заряды возвращаются: {@code dawn}, {@code shortRest},
     *                 {@code longRest}, {@code manual}
     * @param formula  формула возврата («1к6+4»); пусто — восстановить все
     * @param cost     расход одного применения; опускается, если он равен единице
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Uses(int max, int current, String recovery, String formula, Integer cost) {
    }
}
