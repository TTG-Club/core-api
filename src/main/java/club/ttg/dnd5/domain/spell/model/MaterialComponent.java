package club.ttg.dnd5.domain.spell.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MaterialComponent {
    String text;
    Boolean withCost;
    Boolean consumable;
}
