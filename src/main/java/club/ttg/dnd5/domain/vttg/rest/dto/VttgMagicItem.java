package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

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
    /** {@code GameItemType}; для магических предметов — "equipment". */
    private String type;
    /** Отображаемая метка типа (напр. «Снаряжение»). */
    private String typeLabel;
    private int quantity;
    /** Вес в фунтах (в модели источника отсутствует — по умолчанию 0). */
    private double weight;
    /** Стоимость; строковая форма допустима ("" — не задана). */
    private String cost;
    /** {@code ItemRarity}: none, common, uncommon, rare, very-rare, legendary, artifact. */
    private String rarity;
    private boolean equipped;
    /** {@code EquipmentCategory}: wand, ring, wondrous, light, medium, heavy, shield, trinket, clothing... */
    private String equipmentCategory;
    private int baseArmorAC;
    private Integer maxDexBonus;
    private boolean stealthDisadvantage;
    private int strengthRequirement;
    /** {@code magicAttunement}: "none", "required" или "optional". */
    private String magicAttunement;
    /** Источник набора данных: "srd". */
    private String source;
    /** Ключ источника из sources.json: "phb", "dmg", "srd"... */
    private String sourceKey;

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
