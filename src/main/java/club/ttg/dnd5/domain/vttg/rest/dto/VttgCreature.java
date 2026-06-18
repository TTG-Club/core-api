package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

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
}
