package club.ttg.dnd5.dto.species;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeciesFeatureResponse {
    private String url;
    private String name;
    private String english;
    private String alternative;
    private String description;
    private Short page;
    private SourceResponse source;
}