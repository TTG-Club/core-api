package club.ttg.dnd5.domain.beastiary.rest.dto.section;

import club.ttg.dnd5.domain.beastiary.model.section.BeastTreasure;
import club.ttg.dnd5.domain.beastiary.model.section.Habitat;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class BeastSectionRequest extends BaseRequest {
    private Collection<Habitat> habitats;
    private Collection<BeastTreasure> treasures;
}
