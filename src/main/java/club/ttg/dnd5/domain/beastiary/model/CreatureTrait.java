package club.ttg.dnd5.domain.beastiary.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
@Getter
@Setter
public class CreatureTrait {
    private String name;

    private String english;

    private String description;
}