package club.ttg.dnd5.domain.source.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PublisherDto {
    @Schema(description = "Издатель")
    private String name;
    @Schema(description = "Дата издания")
    private LocalDate published;
}