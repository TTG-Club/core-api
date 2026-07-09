package club.ttg.dnd5.domain.article.rest.dto;

import club.ttg.dnd5.domain.article.model.ArticleStatus;
import club.ttg.dnd5.domain.article.model.ArticleType;
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
    @Schema(description = "Идентификатор статьи / новости")
    private UUID id;

    @NotNull
    @Schema(description = "Уникальный url статьи / новости (slug)")
    private String url;

    @NotNull
    @Schema(description = "Тип (ключ): NEWS или ARTICLE")
    private ArticleType type;

    @NotNull
    @Schema(description = "Человеко-читаемое название типа: «Новость» или «Статья»")
    private String typeName;

    @NotNull
    @Schema(description = "Статус (ключ): DRAFT, SCHEDULED, ACTIVE или INACTIVE")
    private ArticleStatus status;

    @NotNull
    @Schema(description = "Человеко-читаемый статус: «Черновик» / «Запланирована» / «Активна» / «Неактивна»")
    private String statusName;

    @Schema(description = "Черновик (не опубликована)")
    private boolean draft;

    @Schema(description = "Активна (в общем доступе); false — неактивна (снята с сайта, но опубликована)")
    private boolean active;

    @NotNull
    @Schema(description = "Заголовок")
    private String title;

    @Nullable
    @Schema(description = "Дата и время публикации")
    private Instant publishDateTime;

    @Schema(description = "Доступна по прямой ссылке даже если не опубликована")
    private boolean accessibleByLink;

    @Schema(description = "Cсылка на превью-изображение")
    @Nullable
    private String previewImageUrl;

    @Schema(description = "Текст превью")
    @NotNull
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String preview;
}
