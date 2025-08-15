package club.ttg.dnd5.domain.beastiary.rest.dto.section;

import club.ttg.dnd5.domain.common.dictionary.CreatureTreasure;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CretureSectionRequest extends BaseRequest {
    private Collection<Habitat> habitats;
    private Collection<CreatureTreasure> treasures;
}
