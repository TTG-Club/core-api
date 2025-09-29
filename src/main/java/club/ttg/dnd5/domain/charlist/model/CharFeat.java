package club.ttg.dnd5.domain.charlist.model;

import club.ttg.dnd5.domain.feat.model.FeatCategory;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CharFeat {
    private FeatCategory category;
    private String name;
    private String description;
    private Collection<AbilityImprovement> abilities;
}
