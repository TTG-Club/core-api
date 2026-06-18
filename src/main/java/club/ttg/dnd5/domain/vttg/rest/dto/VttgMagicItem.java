package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

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
    /** Ключ источника из sources.json: "phb", "dmg", "srd"... */
    private String sourceKey;

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
    @Getter(AccessLevel.NONE)
    private boolean isSRD;
    @Getter(AccessLevel.NONE)
    private boolean isReadOnly;

    @JsonProperty("isMagical")
    public boolean isMagical() {
        return isMagical;
    }

    @JsonProperty("isSRD")
    public boolean isSRD() {
        return isSRD;
    }

    @JsonProperty("isReadOnly")
    public boolean isReadOnly() {
        return isReadOnly;
    }
}
