package club.ttg.dnd5.dto.spell.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("affiliation")
public class SpellAffiliationDTO {
    @JsonProperty("classes")
    private Set<String> classes = new HashSet<>(); // Классы

    @JsonProperty("archetypes")
    private Set<String> archetypes = new HashSet<>(); // Архетипы

    @JsonProperty("species")
    private Set<String> species = new HashSet<>(); // Расы

    @JsonProperty("origins")
    private Set<String> origins = new HashSet<>(); // Происхождения
}
