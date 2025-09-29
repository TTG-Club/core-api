package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassShortResponse extends ShortResponse {
    @Schema(description = "Имеет ли подклассы")
    private boolean hasSubclasses;
}
