package club.ttg.dnd5.domain.article.rest.dto;

import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ArticleRequest {

    @NotNull
    private String url;

    @NotNull
    @Schema(description = "Заголовок")
    private String title;

    @Schema(description = "Cсылка на превью-изображение")
    @Nullable
    private String previewImageUrl;

    @Schema(description = "Текст превью")
    @NotNull
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String preview;

    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @Schema(description = "Текст новости")
    @NotNull
    private String content;
}
