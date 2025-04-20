package club.ttg.dnd5.domain.glossary.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
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
public class GlossaryDetailedResponse extends BaseResponse {
    @NotNull
    private List<String> tags;
}
