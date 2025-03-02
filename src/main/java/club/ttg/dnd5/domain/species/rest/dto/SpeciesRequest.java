package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.beastiary.model.BeastType;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.common.rest.dto.SizeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@Schema(description = "Вид")
public class SpeciesRequest extends BaseRequest {
    private BeastType type;
    @Schema(description = "Размеры существ")
    private Collection<SizeDto> sizes;
    @Schema(description = "Скорость существа")
    private MovementAttributes movementAttributes = new MovementAttributes();
    @Schema(description = "Умения")
    private Collection<FeatureRequest> features;
    @Schema(description = "URL на вид", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String parentUrl;
}
