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
    /** Слаг листа дерева разделов, в котором показывается запись — всегда "feats". */
    private String section;
    /**
     * Раздел сайта в адресе страницы-источника ({@code feats} в {@code /feats/alert-phb}).
     * По паре {@code srcSection}/{@code srcUrl} VTTG находит запись в компендиуме, когда в
     * описании кликают ссылку. {@code srcUrl} не равен {@code id}: тот собирается по схеме
     * эталона ({@code srd_feat_alert}).
     */
    private String srcSection;
    /** Слаг страницы-источника на сайте; с {@code srcSection} составляет адрес ссылки. */
    private String srcUrl;
    /** Ключ источника: "phb"/"dmg"/... */
    private String sourceKey;
    /** Подтип записи в VTTG — всегда "feat". */
    private String featureType;
    /** Человекочитаемая категория для декларативной группировки в компендиуме VTTG. */
    private String category;
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
