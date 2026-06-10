package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgDamagePart {
    private String formula;
    private String type;
    private String target;
    @Getter(AccessLevel.NONE)
    private Boolean isHealing;

    @JsonProperty("isHealing")
    public Boolean getIsHealing() {
        return isHealing;
    }
}
