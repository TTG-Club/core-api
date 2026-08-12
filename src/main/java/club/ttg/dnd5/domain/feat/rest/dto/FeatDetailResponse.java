package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.feat.model.prerequisite.FeatPrerequisite;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FeatDetailResponse extends BaseResponse {
    @Schema(description = "Категория", examples = {"черта происхождения", "общая черта"})
    private String category;
    @Schema(description = "Предварительное условие", examples = {"черта происхождения", "общая черта"})
    private String prerequisite;
    @Schema(description = "Предварительное условие в разобранном виде")
    private FeatPrerequisite prerequisiteDetails;
    @Schema(description = "Механика влияния черты на лист персонажа")
    private FeatMechanics mechanics;
    @Schema(description = "Повторяемость")
    private Boolean repeatability;
    @Schema(description = "Предыстории, дающие эту черту")
    private Collection<FeatBackgroundDto> backgrounds;
}
