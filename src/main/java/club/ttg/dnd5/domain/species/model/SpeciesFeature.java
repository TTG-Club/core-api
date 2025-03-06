package club.ttg.dnd5.domain.species.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpeciesFeature {
    private String url;
    private String name;
    private String english;
    private String description;
}
