package club.ttg.dnd5.domain.character_class.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassFeatureScaling {
    private Integer level;
    private String name;
    private String description;
    private String tooltip;
}
