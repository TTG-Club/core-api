package club.ttg.dnd5.dto.base.source;

import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SourceNameDto extends NameResponse {
    private String label;
}
