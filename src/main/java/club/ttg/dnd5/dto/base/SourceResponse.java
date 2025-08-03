package club.ttg.dnd5.dto.base;

import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName("source")
@Builder
public class SourceResponse {
    private NameResponse name = new NameResponse();
    private NameResponse group = new NameResponse();
    private int page;
}