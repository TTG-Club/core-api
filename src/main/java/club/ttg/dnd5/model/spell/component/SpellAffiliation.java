package club.ttg.dnd5.model.spell.component;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpellAffiliation {
    @ElementCollection
    private List<String> classes;

    @ElementCollection
    private List<String> archetypes;

    @ElementCollection
    private List<String> species;

    @ElementCollection
    private List<String> origins;
}
