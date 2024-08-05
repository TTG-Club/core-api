package club.ttg.dnd5.dto.character;

import club.ttg.dnd5.dto.NameDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TraitResponse {
    private NameDto name;
}
