package club.ttg.dnd5.domain.article.rest.dto;

import club.ttg.dnd5.domain.article.model.ArticleType;
import club.ttg.dnd5.domain.article.model.DiscordMention;
import club.ttg.dnd5.domain.article.model.TelegramPostFormat;
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

    @Schema(description = "Вид поста в Telegram: INSTANT_VIEW (по умолчанию) — одно сообщение с карточкой "
            + "Instant View, полный текст открывается в Telegram по кнопке на ней; FULL_TEXT — прежний вид, "
            + "полный текст в самом посте (при необходимости несколькими сообщениями) с обложкой. Учитывается "
            + "только при publishToTelegram=true. INSTANT_VIEW без настроенного шаблона (telegram.instant-view-rhash) "
            + "или без публичного адреса сайта автоматически публикуется как FULL_TEXT.")
    @Nullable
    private TelegramPostFormat telegramFormat;

    @Schema(description = "Добавить короткое описание (выжимку) под карточкой Instant View. Учитывается "
            + "только при publishToTelegram=true и telegramFormat=INSTANT_VIEW: пост уходит карточкой, "
            + "а под ней — текст из telegramSummary. false — карточка без текста, как прежде.")
    private boolean telegramSummaryEnabled;

    @Schema(description = "Текст короткого описания под карточкой Instant View. Учитывается только при "
            + "telegramSummaryEnabled=true; пустой текст равнозначен выключенной галочке. Обычный текст "
            + "(в админке — текстовое поле): переносы строк сохраняются, поддержаны markdown-выделение "
            + "(**жирный**, *курсив*) и маркеры {@…} — в пост уходит Telegram-HTML.")
    @Nullable
    private String telegramSummary;

    @Schema(description = "Опубликовать в Discord-канал. true — при публикации (сейчас или по наступлении даты) "
            + "запись один раз уйдёт в канал, если интеграция включена глобально. false — в канал не отправлять. "
            + "Независима от Telegram.")
    private boolean publishToDiscord;

    @Schema(description = "Пинг участников Discord-канала при публикации: NONE (по умолчанию) — без пинга, "
            + "EVERYONE — @everyone, SERVER — роль server. Учитывается только при publishToDiscord=true и "
            + "только в первом сообщении поста; правка поста повторно не пингует.")
    @Nullable
    private DiscordMention discordMention;

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
