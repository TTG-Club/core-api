package club.ttg.dnd5.domain.character_class.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassFeature {
    private String key;
    private Integer level;
    private String name;
    private String description;
    private String tooltip;
    List<ClassFeatureScaling> scaling;
}
