package club.ttg.dnd5.domain.species.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.BeastType;
import club.ttg.dnd5.domain.common.rest.dto.BaseDto;
import club.ttg.dnd5.domain.common.rest.dto.SizeDto;
import club.ttg.dnd5.domain.species.rest.dto.FeatureRequest;
import club.ttg.dnd5.domain.species.rest.dto.MovementAttributes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@Schema(description = "Вид")
public class SpeciesRequest extends BaseDto {
    private BeastType type;
    @Schema(description = "Размеры существ")
    private Collection<SizeDto> sizes;
    @Schema(description = "Скорость существа")
    private MovementAttributes movementAttributes = new MovementAttributes();
    private Collection<FeatureRequest> features;
}
