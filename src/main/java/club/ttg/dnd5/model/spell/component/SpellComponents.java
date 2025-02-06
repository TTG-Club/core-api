package club.ttg.dnd5.model.spell.component;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
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
public class SpellComponents {
    private boolean verbal;
    private boolean somatic;

    @ElementCollection
    @CollectionTable(name = "spell_materials", joinColumns = @JoinColumn(name = "spell_id"))
    private List<SpellMaterialComponent> material;
}
