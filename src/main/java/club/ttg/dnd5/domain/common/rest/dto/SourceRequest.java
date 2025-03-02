package club.ttg.dnd5.domain.common.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SourceRequest {
    private String url;
    private int page;
}
