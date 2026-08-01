package club.ttg.dnd5.domain.vttg.rest.dto;

import club.ttg.dnd5.domain.spell.model.SpellActiveEffect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgSpell {
    private String id;
    private String name;
    private String nameEn;
    private long level;
    private String school;
    private long castingTimeValue;
    private String castingTimeUnit;
    private String reactionTrigger;
    private VttgSpellComponents components;
    private long range;
    private String rangeUnit;
    private String rangeSpecial;
    private long durationValue;
    private String durationUnit;
    private boolean concentration;
    private boolean ritual;
    private VttgSpellAreaOfEffect areaOfEffect;
    private String targetType;
    private Integer targetCount;
    private String deliveryType;
    private List<VttgDamagePart> damageParts;
    private Boolean autoHit;
    private String saveType;
    private String saveEffect;
    /**
     * Активные эффекты заклинания (система Active Effects VTTG) — например помеха на атаку у
     * «Злой насмешки». Передаются без преобразования (модель уже в вокабуляре VTTG).
     */
    private List<SpellActiveEffect> activeEffects;
    private String cantripScaling;
    private List<VttgCantripScalingTier> cantripScalingTiers;
    private VttgSpellScaling scaling;
    private String description;
    private String higherLevelDescription;
    private String sourceKey;
    @Getter(AccessLevel.NONE)
    private boolean isSRD;
    private List<String> classKeys;
    private String type;
    /** Slug листа дерева разделов, в котором показывается запись (всегда "spells"). */
    private String section;
    /**
     * Раздел сайта в адресе страницы-источника ({@code items} в {@code /items/dagger-phb}).
     * По паре {@code srcSection}/{@code srcUrl} VTTG находит запись в компендиуме, когда в
     * описании кликают ссылку. С {@code section} совпадает не всегда: там лист дерева
     * компендиума, здесь раздел сайта.
     */
    private String srcSection;
    /** Слаг страницы-источника на сайте; с {@code srcSection} составляет адрес ссылки. */
    private String srcUrl;

    @JsonProperty("isSRD")
    public boolean isSRD() {
        return isSRD;
    }
}
