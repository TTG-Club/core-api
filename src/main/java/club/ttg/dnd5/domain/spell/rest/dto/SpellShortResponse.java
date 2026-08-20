package club.ttg.dnd5.domain.spell.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

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
    private Set<SpellAffiliationDto> classes;
    /**
     * Характеристика заклинания из {@code effect.spellcastingAbility}; {@code null} —
     * не задана, и потребитель берёт характеристику класса. Едет в кратком ответе,
     * потому что лист персонажа собирает книгу заклинаний именно из него.
     */
    private String spellcastingAbility;

}
