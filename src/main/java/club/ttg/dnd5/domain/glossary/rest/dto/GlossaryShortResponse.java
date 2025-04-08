package club.ttg.dnd5.domain.glossary.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GlossaryShortResponse extends ShortResponse {
    @NotNull
    private String tags;
}
