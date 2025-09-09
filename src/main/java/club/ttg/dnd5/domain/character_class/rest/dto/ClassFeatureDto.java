package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.character_class.model.ClassFeature;
import club.ttg.dnd5.domain.character_class.model.ClassFeatureScaling;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassFeatureDto extends ClassFeature {
    @Schema(description = "Являетя ли способность подклассовой")
    private Boolean isSubclass;

    public ClassFeatureDto(ClassFeature feature, Boolean isSubclass) {
        super(feature.getKey(), feature.getLevel(), feature.getName(), feature.getDescription(), feature.getTooltip(), feature.getScaling());
        this.isSubclass = isSubclass;
    }
}
