package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

/**
 * Черта (feat) в формате компендиума VTTG.
 *
 * <p>Соответствует целевому формату SRD-бэкапа VTTG (см. {@code feats.json}): самоописывающаяся
 * запись с {@code id}/{@code type}/{@code isSRD}, постоянными {@code featureType = "feat"} и
 * {@code typeLabel = "Черты"} и флагом {@code repeatable} (повторяемость черты).</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgFeat {
    private String id;
    private String name;
    private String nameEn;
    /** Канонический тип сущности для VTTG — всегда "feat". */
    private String type;
    /** Человекочитаемое имя источника, например "PHB 2024". */
    private String source;
    /** Ключ источника: "phb"/"dmg"/... */
    private String sourceKey;
    /** Подтип записи в VTTG — всегда "feat". */
    private String featureType;
    private String description;
    /** Метка типа для отображения — всегда "Черты". */
    private String typeLabel;

    @Getter(AccessLevel.NONE)
    private boolean isSRD;
    @Getter(AccessLevel.NONE)
    private boolean repeatable;

    @JsonProperty("isSRD")
    public boolean isSRD() {
        return isSRD;
    }

    @JsonProperty("repeatable")
    public boolean isRepeatable() {
        return repeatable;
    }
}
