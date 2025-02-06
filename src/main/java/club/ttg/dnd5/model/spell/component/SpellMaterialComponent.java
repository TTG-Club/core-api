package club.ttg.dnd5.model.spell.component;

import club.ttg.dnd5.model.spell.enums.ComparisonOperator;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpellMaterialComponent {
    private String name;
    private Integer price;

    @Enumerated(EnumType.STRING)
    private ComparisonOperator comparison; // <, >, =

    private boolean consumable;
}
