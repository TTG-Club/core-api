package club.ttg.dnd5.domain.spell.rest.dto.create;

import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.spell.model.*;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SpellRequest extends BaseRequest {
    @Min(0)
    @Max(9)
    @NotNull
    private Long level;
    @NotNull
    private MagicSchool school;
    @NotNull
    private SpellComponents components;
    @NotEmpty
    private List<SpellCastingTime> castingTime;
    @NotEmpty
    private List<SpellDistance> range;
    @NotEmpty
    private List<SpellDuration> duration;
    @Nullable
    private String upper;
    @Nullable
    private CreateAffiliationRequest affiliations;
}
