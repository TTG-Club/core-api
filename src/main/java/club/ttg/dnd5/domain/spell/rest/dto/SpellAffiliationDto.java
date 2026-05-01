package club.ttg.dnd5.domain.spell.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SpellAffiliationDto implements Comparable<SpellAffiliationDto>{
    private String url;
    private String name;
    @JsonIgnore
    private String source;

    @Override
    public int compareTo(@NonNull final SpellAffiliationDto o) {
        var comp = name.compareTo(o.name);
        if (name.compareTo(o.name) == 0) {
            return source.compareTo(o.source);
        }
        return comp;
    }
}
