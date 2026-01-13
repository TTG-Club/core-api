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

import java.time.Instant;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ArticleShortResponse {
    @NotNull
    private UUID id;

    @NotNull
    private String url;

    @NotNull
    @Schema(description = "Заголовок")
    private String title;

    @Nullable
    @Schema(description = "Дата и время публикации")
    private Instant publishDateTime;

    @Schema(description = "Cсылка на превью-изображение")
    @Nullable
    private String previewImageUrl;

    @Schema(description = "Текст превью")
    @NotNull
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String preview;
}
