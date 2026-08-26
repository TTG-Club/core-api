package club.ttg.dnd5.domain.vttg.rest.dto;

import club.ttg.dnd5.domain.common.model.ActiveEffect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgCreature {
    private String id;
    private String entityType;
    private String type;
    /** Slug листа дерева разделов, в котором показывается запись (всегда "creatures"). */
    private String section;
    /**
     * Раздел сайта в адресе страницы-источника ({@code bestiary} в {@code /bestiary/goblin-mm}).
     * По паре {@code srcSection}/{@code srcUrl} VTTG находит запись в компендиуме, когда в
     * описании кликают ссылку. С {@code section} не совпадает: там лист дерева компендиума
     * ({@code creatures}), здесь раздел сайта.
     */
    private String srcSection;
    /** Слаг страницы-источника на сайте; с {@code srcSection} составляет адрес ссылки. */
    private String srcUrl;
    private Boolean autoSaves;
    private String name;
    private String nameEn;
    private String description;
    private String header;
    private Map<String, Object> token;
    private Map<String, Object> system;
    private String sourceKey;
    private Boolean isSRD;
    private Boolean isReadOnly;

    /**
     * Активные эффекты существа в вокабуляре VTTG — та же модель, что у черты и предмета.
     *
     * <p>Соседом {@link #system}, а не его полем: статблок описывает существо числами, а
     * эффект — то, чем существо постоянно отличается от своих чисел или что оно
     * накладывает. На столе такой эффект уезжает на токен как есть.</p>
     */
    private List<ActiveEffect> activeEffects;
}
