package club.ttg.dnd5.domain.magic.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MagicItemShortResponse extends ShortResponse {
    private String rarity;
    private boolean attunement;
}
