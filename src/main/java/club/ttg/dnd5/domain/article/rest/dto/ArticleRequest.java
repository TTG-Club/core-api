package club.ttg.dnd5.domain.article.rest.dto;

import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import club.ttg.dnd5.dto.base.serializer.FormattedMarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

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

    @Nullable
    @Schema(description = "Дата и время публикации")
    private Instant publishDateTime;

    @Schema(description = "Текст превью")
    @NotNull
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @JsonSerialize(using = FormattedMarkupDescriptionSerializer.class)
    private String preview;

    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @JsonSerialize(using = FormattedMarkupDescriptionSerializer.class)
    @Schema(description = "Текст новости")
    @NotNull
    private String content;
}
