package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.character_class.rest.dto.ClassFeatureOptionRequest;
import club.ttg.dnd5.domain.common.rest.dto.Name;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import club.ttg.dnd5.util.SlugifyUtil;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class ClassFeatureOption {

    @Schema(description = "Stable option slug", example = "agonizing_blast")
    private String key;

    @Schema(description = "Option name")
    private Name name;

    @Schema(description = "Option description")
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String description;

    @Schema(description = "Short additional label")
    private String additional;

    @Schema(description = "Option prerequisite")
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String prerequisite;

    @Schema(description = "Required class level for this option")
    private Integer requiredClassLevel;

    @Schema(description = "Hide option in subclass and multiclass contexts")
    private boolean hideInSubclasses;

    public ClassFeatureOption(ClassFeatureOptionRequest request) {
        this.key = StringUtils.hasText(request.getKey()) ? request.getKey() : buildKey(request.getName());
        this.name = request.getName();
        this.description = request.getDescription();
        this.additional = request.getAdditional();
        this.prerequisite = request.getPrerequisite();
        this.requiredClassLevel = request.getRequiredClassLevel();
        this.hideInSubclasses = request.isHideInSubclasses();
    }

    public ClassFeatureOption(ClassFeatureOption option) {
        this.key = option.getKey();
        this.name = option.getName();
        this.description = option.getDescription();
        this.additional = option.getAdditional();
        this.prerequisite = option.getPrerequisite();
        this.requiredClassLevel = option.getRequiredClassLevel();
        this.hideInSubclasses = option.isHideInSubclasses();
    }

    private String buildKey(Name name) {
        if (name == null) {
            return null;
        }

        String source = StringUtils.hasText(name.getEnglish()) ? name.getEnglish() : name.getName();
        return StringUtils.hasText(source) ? SlugifyUtil.getSlug(source).replace('-', '_') : null;
    }
}
