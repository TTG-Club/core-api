package club.ttg.dnd5.domain.communitynews.rest.dto;

import club.ttg.dnd5.domain.common.validation.VideoUrl;
import club.ttg.dnd5.domain.common.validation.WebUrl;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import club.ttg.dnd5.dto.base.serializer.FormattedMarkupOrEmptySerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Тело создания / правки / предпросмотра новости сообщества и ответ формы для правки ({@code /raw}).
 * В {@code /raw} пустые {@code preview}/{@code content}/{@code authorName} отдаются {@code ""}, а не
 * {@code null}, — форму можно сразу отправить обратно без правок.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CommunityNewsRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Уникальный url новости сообщества (slug)")
    private String url;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Заголовок")
    private String title;

    @Size(max = 128)
    @Schema(description = "Автор (кто прислал новость / чья она). Может быть пустой строкой — автор не указан")
    private String authorName;

    @Nullable
    @WebUrl
    @Schema(description = "Ссылка на первоисточник: только http(s)://, до 512 символов. Пусто или null — нет")
    private String sourceUrl;

    @Nullable
    @Schema(description = "Ссылка на превью-изображение (заставка плеера, если есть видео)")
    private String previewImageUrl;

    @Nullable
    @VideoUrl
    @Schema(description = "Ссылка на ролик YouTube или ВК Видео (только https://, до 512 символов). На сайте "
            + "показывается вместо обложки. Пусто или null — видео нет")
    private String videoUrl;

    @Schema(description = "Черновик. true — не опубликована: видна только редакторам в списке неопубликованных, "
            + "недоступна ни на сайте, ни по ссылке. false — опубликована")
    private boolean draft;

    @Schema(description = "Активность опубликованной записи (учитывается при draft=false). true — в общем доступе "
            + "(с учётом даты публикации); false — снята с сайта, но остаётся опубликованной")
    private boolean active;

    @Schema(description = "Доступна по прямой ссылке, даже когда не в общем доступе. Актуально для неактивной "
            + "опубликованной записи; к черновику не относится")
    private boolean accessibleByLink;

    @Nullable
    @Schema(description = "Дата публикации. Будущая дата при draft=false, active=true — запись запланирована и "
            + "появится на сайте сама. Если не задана при публикации — ставится «сейчас»")
    private Instant publishDateTime;

    @NotBlank
    @Schema(description = "Текст превью (разметка)")
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @JsonSerialize(using = FormattedMarkupOrEmptySerializer.class, nullsUsing = FormattedMarkupOrEmptySerializer.class)
    private String preview;

    @Schema(description = "Полный текст (разметка). Может быть пустым — новость состоит из одного превью")
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @JsonSerialize(using = FormattedMarkupOrEmptySerializer.class, nullsUsing = FormattedMarkupOrEmptySerializer.class)
    private String content;
}
