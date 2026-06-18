package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Предыстория (background) в формате компендиума VTTG.
 *
 * <p>Соответствует целевому формату SRD-бэкапа VTTG (см. {@code backgrounds/*.json}):
 * самоописывающаяся запись с {@code id}/{@code type}/{@code isSRD} и блоками наград
 * ({@code abilityGrant}/{@code skillGrant}/{@code featGrant}/{@code equipmentOptions}).</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgBackground {
    private String id;
    /** Дублирует {@code id} (как в эталоне). */
    private String key;
    private String name;
    private String nameEn;
    private String description;
    /** Ключ источника: "phb"/"dmg"/... */
    private String sourceKey;
    private AbilityGrant abilityGrant;
    private SkillGrant skillGrant;
    private FeatGrant featGrant;
    private List<EquipmentOption> equipmentOptions;
    /** Канонический тип сущности для VTTG — всегда "background". */
    private String type;

    @Getter(AccessLevel.NONE)
    private boolean isSRD;

    @JsonProperty("isSRD")
    public boolean isSRD() {
        return isSRD;
    }

    /** Бонусные характеристики (slug'и: "strength".."charisma"). */
    public record AbilityGrant(List<String> abilities) {
    }

    /** Владения навыками (camelCase slug'и: "sleightOfHand", "insight"...). */
    public record SkillGrant(List<String> skills) {
    }

    /** Даруемая черта. */
    public record FeatGrant(String featId, String featName, String featNameEn) {
    }

    /** Вариант стартового снаряжения; {@code goldAlternative} — альтернатива золотом (опц.). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EquipmentOption(String description, Integer goldAlternative) {
    }
}
