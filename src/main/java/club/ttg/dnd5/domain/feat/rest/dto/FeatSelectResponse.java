package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.rest.dto.NameRequest;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.dto.base.SourceResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
public class FeatSelectResponse{
    @Schema(description = "unique URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;
    @JsonProperty(value = "name")
    @Schema(description = "название", requiredMode = Schema.RequiredMode.REQUIRED)
    private NameRequest name;
    @Schema(description = "Категория черты", examples = {"ORIGIN", "GENERAL", "EPIC_BOON", "FIGHTING_STYLE"})
    private FeatCategory category;
    @Schema(description = "Предварительное условие")
    private String prerequisite;
    @Schema(description = "Повторяемость")
    private Boolean repeatability;
    @Schema(description = "Улучшаемые характеристики", examples = {"STRENGTH", "DEXTERITY", "CONSTITUTION"})
    private Collection<Ability> abilities;
    @Schema(description = "Количество улучшаемых характеристик")
    private int increase;

    @Schema(description = "источник", requiredMode = Schema.RequiredMode.REQUIRED)
    private SourceResponse source;
}
