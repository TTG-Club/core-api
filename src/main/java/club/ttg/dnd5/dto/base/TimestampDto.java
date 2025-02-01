package club.ttg.dnd5.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TimestampDto {
    @Schema(description = "Дата и время создания")
    private Instant createdAt;

    @Schema(description = "Дата и время последнего обновления")
    private Instant updatedAt;
}
