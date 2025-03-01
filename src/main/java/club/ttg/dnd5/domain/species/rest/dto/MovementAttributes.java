package club.ttg.dnd5.domain.species.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovementAttributes{
    private final int base = 30;
    private Integer fly;
    private Integer climb;
    private Integer swim;
    private Boolean hover;
}
