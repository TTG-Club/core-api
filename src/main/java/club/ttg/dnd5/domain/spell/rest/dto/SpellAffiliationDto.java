package club.ttg.dnd5.domain.spell.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SpellAffiliationDto implements Comparable<SpellAffiliationDto>
{
    public static final Comparator<SpellAffiliationDto> BY_NAME_THEN_SOURCE = Comparator
            .comparing(SpellAffiliationDto::getName, Comparator.nullsFirst(String::compareTo))
            .thenComparing(SpellAffiliationDto::getSource, Comparator.nullsFirst(String::compareTo))
            .thenComparing(SpellAffiliationDto::getUrl, Comparator.nullsFirst(String::compareTo));

    private String url;
    private String name;
    @JsonIgnore
    private String source;

    @Override
    public int compareTo(@NonNull final SpellAffiliationDto o)
    {
        return BY_NAME_THEN_SOURCE.compare(this, o);
    }
}
