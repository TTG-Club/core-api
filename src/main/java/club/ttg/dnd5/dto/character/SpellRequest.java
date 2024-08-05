package club.ttg.dnd5.dto.character;

import club.ttg.dnd5.dto.NameDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SpellRequest {
    private NameDto name;
}
