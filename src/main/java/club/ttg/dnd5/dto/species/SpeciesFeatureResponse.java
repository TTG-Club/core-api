package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.EntryDto;
import club.ttg.dnd5.dto.base.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeciesFeatureResponse extends BaseDTO {
    @Schema(description = "описание", requiredMode = Schema.RequiredMode.REQUIRED)
    private EntryDto entries;
}