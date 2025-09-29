package club.ttg.dnd5.domain.charlist.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CharBackground {
    private String name;
    private String description;
    private Collection<AbilityImprovement> abilities;
}
