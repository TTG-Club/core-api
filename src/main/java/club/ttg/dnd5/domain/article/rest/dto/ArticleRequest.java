package club.ttg.dnd5.domain.article.rest.dto;

import club.ttg.dnd5.domain.article.model.ArticleType;
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
    @Schema(description = "Уникальный url статьи / новости (slug)")
    private String url;

    @NotNull
    @Schema(description = "Тип: NEWS (новость) или ARTICLE (статья)")
    private ArticleType type;

    @Schema(description = "Черновик. true — сохранить черновиком (не публиковать): виден только в списке "
            + "черновиков, недоступен ни на сайте, ни по ссылке. false — опубликована (не черновик).")
    private boolean draft;

    @Schema(description = "Активность опубликованной записи (учитывается при draft=false). true — активна "
            + "(в общем доступе на сайте, с учётом даты публикации); false — неактивна (снята с сайта, "
            + "но остаётся опубликованной, не черновик).")
    private boolean active;

    @NotNull
    @Schema(description = "Заголовок")
    private String title;

    @Schema(description = "Cсылка на превью-изображение")
    @Nullable
    private String previewImageUrl;

    @Nullable
    @Schema(description = "Дата публикации. Будущая дата при draft=false, active=true — запись запланирована "
            + "и появится на сайте автоматически по её наступлении. Если не задана при публикации — ставится «сейчас».")
    private Instant publishDateTime;

    @Schema(description = "Доступна по прямой ссылке, даже когда не в общем доступе (для предпросмотра/шеринга). "
            + "Актуально для неактивной опубликованной записи (draft=false, active=false); к черновику не относится.")
    private boolean accessibleByLink;

    @Schema(description = "Опубликовать в Telegram-канал. true — при публикации (сейчас или по наступлении даты) "
            + "запись один раз уйдёт в канал, если интеграция включена глобально. false — в канал не отправлять.")
    private boolean publishToTelegram;

    @Schema(description = "Опубликовать в Discord-канал. true — при публикации (сейчас или по наступлении даты) "
            + "запись один раз уйдёт в канал, если интеграция включена глобально. false — в канал не отправлять. "
            + "Независима от Telegram.")
    private boolean publishToDiscord;

    @Schema(description = "Опубликовать на стену сообщества ВКонтакте. true — при публикации (сейчас или по "
            + "наступлении даты) запись один раз уйдёт на стену, если интеграция включена глобально. false — "
            + "на стену не отправлять. Независима от Telegram и Discord.")
    private boolean publishToVk;

    @Schema(description = "Текст превью")
    @NotNull
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @JsonSerialize(using = FormattedMarkupDescriptionSerializer.class)
    private String preview;

    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @JsonSerialize(using = FormattedMarkupDescriptionSerializer.class)
    @Schema(description = "Текст статьи / новости")
    @NotNull
    private String content;
}
