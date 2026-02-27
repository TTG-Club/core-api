package club.ttg.dnd5.domain.source.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TranslationDto {
    @Schema(description = "Переводчики")
    private List<String> authors;
    @Schema(description = "Дата перевода")
    private LocalDate date;
}