package club.ttg.dnd5.domain.vttg.rest.dto;

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
    private String damageFormula;
    private String damageType;
    private List<VttgDamagePart> damageParts;
    private Boolean isHealing;
    private Boolean autoHit;
    private String saveType;
    private String saveEffect;
    private String cantripScaling;
    private List<VttgCantripScalingTier> cantripScalingTiers;
    private VttgSpellScaling scaling;
    private String description;
    private String higherLevelDescription;
    private String source;
    private String sourceKey;
    @Getter(AccessLevel.NONE)
    private boolean isSRD;
    private List<String> classKeys;
    private String type;

    @JsonProperty("isSRD")
    public boolean isSRD() {
        return isSRD;
    }
}
