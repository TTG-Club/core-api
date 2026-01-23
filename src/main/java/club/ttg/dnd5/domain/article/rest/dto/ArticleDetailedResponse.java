package club.ttg.dnd5.domain.article.rest.dto;

import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
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
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ArticleDetailedResponse {
    @NotNull
    private UUID id;

    @NotNull
    private String url;

    @Nullable
    @Schema(description = "Дата и время публикации")
    private Instant publishDateTime;

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

    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    @Schema(description = "Текст новости")
    @NotNull
    private String content;

    @Schema(description = "дата обновления")
    @NotNull
    private String updatedAt;

    @Schema(description = "дата Добавления")
    @NotNull
    private String createdAt;
}
