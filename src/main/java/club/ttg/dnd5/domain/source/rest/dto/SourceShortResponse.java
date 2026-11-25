package club.ttg.dnd5.domain.source.rest.dto;

import club.ttg.dnd5.domain.common.model.ImageFrame;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SourceShortResponse extends ShortResponse {
    private String image;

    @Schema(description = "Кадр обложки; null — не настроен")
    private ImageFrame imageFrame;
}
