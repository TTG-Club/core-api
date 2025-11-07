package club.ttg.dnd5.domain.species.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class SpeciesFeature {
    private String url;
    private String name;
    private String english;
    private String description;
}
