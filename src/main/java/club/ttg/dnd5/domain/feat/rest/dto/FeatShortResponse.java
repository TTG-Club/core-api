package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeatShortResponse extends ShortResponse {
    private String category;
}
