package club.ttg.dnd5.domain.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortResponse {
    private String url;
    private NameDto name = new NameDto();
}