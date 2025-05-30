package club.ttg.dnd5.domain.common.rest.dto.rating;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingRequest {
    private String section;
    private String url;
    private byte value;
}
