package club.ttg.dnd5.domain.spell.rest.dto.create;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CreateAffiliationRequest {
    @Nullable
    private Set<String> classes;
    @Nullable
    private Set<String> subclasses;
    @Nullable
    private Set<String> species;
    @Nullable
    private Set<String> lineages;
    @Nullable
    private Set<String> feats;
}
