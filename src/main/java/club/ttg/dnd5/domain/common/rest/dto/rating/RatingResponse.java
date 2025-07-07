package club.ttg.dnd5.domain.common.rest.dto.rating;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingResponse {
    private double value;
    private long total;
}
