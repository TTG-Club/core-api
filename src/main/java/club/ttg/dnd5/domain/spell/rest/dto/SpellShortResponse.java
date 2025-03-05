package club.ttg.dnd5.domain.spell.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SpellShortResponse extends ShortResponse {
    private Long level;
    private String school;
    private String additionalType;
    private Boolean concentration;
    private Boolean ritual;
    private SpellShortComponents components;

}
