package club.ttg.dnd5.domain.spell.rest.dto.create;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateAffiliationRequest {
    @Nullable
    private List<String> classes;
    @Nullable
    private List<String> subclasses;
    @Nullable
    private List<String> species;
    @Nullable
    private List<String> lineages;
}
