package club.ttg.dnd5.domain.token.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenBorderResponse {
    private String id;
    private int order;
    private String url;
}
