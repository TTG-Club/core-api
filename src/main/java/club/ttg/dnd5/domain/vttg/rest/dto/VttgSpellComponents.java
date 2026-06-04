package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgSpellComponents {
    private boolean verbal;
    private boolean somatic;
    private boolean material;
    private String materialDescription;
    private Boolean materialConsumed;
}
