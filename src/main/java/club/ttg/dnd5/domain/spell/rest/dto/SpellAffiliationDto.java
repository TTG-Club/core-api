package club.ttg.dnd5.domain.spell.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SpellAffiliationDto {
    private String url;
    private String name;
    @JsonIgnore
    private String source;
}
