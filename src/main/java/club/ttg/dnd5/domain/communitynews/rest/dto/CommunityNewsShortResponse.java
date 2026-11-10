package club.ttg.dnd5.domain.communitynews.rest.dto;

import club.ttg.dnd5.domain.communitynews.model.CommunityNewsStatus;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class CommunityNewsShortResponse {
    @NotNull
    @Schema(description = "Идентификатор новости сообщества")
    private UUID id;

    @NotNull
    @Schema(description = "Уникальный url новости сообщества (slug)")
    private String url;

    @NotNull
    @Schema(description = "Заголовок")
    private String title;

    @Nullable
    @Schema(description = "Автор (кто прислал новость / чья она)")
    private String authorName;

    @Nullable
    @Schema(description = "Ссылка на первоисточник")
    private String sourceUrl;

    @Nullable
    @Schema(description = "Ссылка на превью-изображение")
    private String previewImageUrl;

    @Nullable
    @Schema(description = "Ссылка на ролик YouTube или ВК Видео; показывается вместо обложки")
    private String videoUrl;

    @Schema(description = "Черновик (не опубликована)")
    private boolean draft;

    @Schema(description = "Активна (в общем доступе); false — снята с сайта, но опубликована")
    private boolean active;

    @NotNull
    @Schema(description = "Статус (ключ): DRAFT, SCHEDULED, ACTIVE или INACTIVE")
    private CommunityNewsStatus status;

    @NotNull
    @Schema(description = "Человеко-читаемый статус: «Черновик» / «Запланирована» / «Активна» / «Неактивна»")
    private String statusName;

    @Schema(description = "Доступна по прямой ссылке даже если не в общем доступе")
    private boolean accessibleByLink;

    @Nullable
    @Schema(description = "Дата и время публикации")
    private Instant publishDateTime;

    @NotNull
    @Schema(description = "Текст превью (разметка)")
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String preview;
}
