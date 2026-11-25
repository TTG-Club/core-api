package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.model.ImageFrame;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Виды и происхождения кратко")
public class SpeciesShortResponse extends ShortResponse {
    @Schema(description = "Ссылка на изображение бэкграунда")
    private String image;

    @Schema(description = "Кадр обложки; null — не настроен")
    private ImageFrame imageFrame;
    private boolean hasLineages = false;
}
