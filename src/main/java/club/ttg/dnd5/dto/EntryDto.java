package club.ttg.dnd5.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EntryDto {
    @Schema(description = "тип", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;
    @Schema(description = "название", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(description = "записи", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Object> entries;
}
