package club.ttg.dnd5.domain.feat.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ссылка на предысторию, дающую черту.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FeatBackgroundDto {
    @Schema(description = "URL предыстории", examples = "akolit")
    private String url;
    @Schema(description = "Название предыстории", examples = "Аколит [PHB24]")
    private String name;
}
