package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.config.properties.TelegramProperties;
import club.ttg.dnd5.domain.article.model.Article;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Отправляет и синхронизирует пост статьи / новости в Telegram-канале через Bot API.
 * <p>
 * Ничего не знает про БД и транзакции — только строит текст/пейлоад и делает HTTP-вызов. Длинный пост не
 * обрезается: он уходит одним текстовым сообщением (≤4096) с обложкой, показанной превью-ссылкой НАД текстом
 * ({@code link_preview_options.show_above_text}), а остаток досылается отдельными сообщениями (≤4096). Если
 * публичный URL обложки собрать нельзя ({@code app.url} не публичный) — фоллбэк на фото с подписью (≤1024).
 * Текст поста — Telegram-HTML (см. {@link TelegramHtmlFormatter}), обложку для фото-фоллбэка заливаем файлом
 * из S3 (см. {@link ArticleImageSource}). Итог отправки — явный статус, чтобы планировщик отличал «повторить»
 * (временный сбой) от «сдаться» (перманентный отказ Telegram).
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TelegramPublisher {

    /** Лимиты Bot API: подпись к фото и обычное сообщение (в символах). */
    private static final int CAPTION_LIMIT = 1024;
    private static final int MESSAGE_LIMIT = 4096;

    /** Итог одного вызова Bot API. */
    private enum SendResult {
        /** 2xx — отправлено. */
        SENT,
        /** 4xx (кроме 429) — запрос неверен навсегда (битая картинка/разметка/чат/«не изменено»): повтор бесполезен. */
        REJECTED,
        /** 429/5xx/сеть/таймаут — временно: имеет смысл повторить. */
        TRANSIENT
    }

    /**
     * Итог отправки нового поста: {@code messageId} — id ПЕРВОГО сообщения (фото/основного),
     * {@code tailMessageIds} — id хвостовых сообщений многочастного поста (для удаления всего поста).
     */
    public record PublishResult(Status status, Long messageId, boolean photo, List<Long> tailMessageIds) {
        public enum Status { POSTED, RETRY, GIVE_UP }

        static PublishResult posted(Long messageId, boolean photo, List<Long> tailMessageIds) {
            return new PublishResult(Status.POSTED, messageId, photo, tailMessageIds);
        }

        static PublishResult retry() {
            return new PublishResult(Status.RETRY, null, false, List.of());
        }

        static PublishResult giveUp() {
            return new PublishResult(Status.GIVE_UP, null, false, List.of());
        }

        /** Хвостовые id → CSV для хранения ({@code null}, если хвоста нет). */
        public String tailMessageIdsCsv() {
            return tailMessageIds == null || tailMessageIds.isEmpty()
                    ? null
                    : tailMessageIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
    }

    /** Итог синхронизации правки поста. */
    public enum EditResult { SYNCED, RETRY, GIVE_UP }

    private final RestClient telegramRestClient;
    private final TelegramProperties properties;
    private final TelegramHtmlFormatter formatter;
    private final ArticleImageSource imageSource;

    /** База для абсолютного URL обложки в превью-режиме (см. {@link #coverUrl}). */
    @Value("${app.url:https://ttg.club}")
    private String appUrl;

    /**
     * Отправляет новый пост в канал (при необходимости — несколькими сообщениями).
     */
    public PublishResult publish(Article article) {
        if (!StringUtils.hasText(article.getTitle()) && !StringUtils.hasText(formatter.toPlain(description(article)))) {
            log.warn("Пустой заголовок и текст — пропускаю отправку {} в Telegram", article.getUrl());
            return PublishResult.giveUp();
        }

        // Обложку показываем превью-ссылкой НАД текстом (link_preview_options.show_above_text): пост уходит
        // одним текстовым сообщением (первый кусок — до 4096, а не 1024 как в подписи к фото). Требует
        // публичного URL обложки — Telegram качает её сам. Если URL не публичный — фоллбэк на фото ниже.
        String cover = coverUrl(article.getPreviewImageUrl());
        if (cover != null) {
            List<String> messages = buildMessages(article, MESSAGE_LIMIT);
            SendOutcome first = send("sendMessage", linkPreviewPayload(messages.get(0), cover));
            if (first.result() != SendResult.SENT) {
                return first.result() == SendResult.TRANSIENT ? PublishResult.retry() : PublishResult.giveUp();
            }
            List<Long> tailIds = sendTail(article, messages);
            // photo=false: пост текстовый (правку делаем editMessageText), обложка — превью ссылки, не фото.
            return PublishResult.posted(first.messageId(), false, tailIds);
        }
        if (StringUtils.hasText(article.getPreviewImageUrl())) {
            // Обложка есть, но публичный URL не собрать (app.url не публичный — напр. локально localhost):
            // превью не сработает (Telegram не скачает картинку) — уходим на фото с подписью ниже.
            log.info("Публичный URL обложки не собран для {} (app.url не публичный?) — отправляю фото с подписью ≤{}",
                    article.getUrl(), CAPTION_LIMIT);
        }

        boolean wantPhoto = StringUtils.hasText(article.getPreviewImageUrl());
        List<String> messages = buildMessages(article, wantPhoto ? CAPTION_LIMIT : MESSAGE_LIMIT);

        // Первое сообщение: фото с подписью либо текст.
        SendOutcome first = null;
        boolean photo = false;
        if (wantPhoto) {
            SendOutcome sent = trySendPhoto(article, messages.get(0));
            if (sent != null) {
                if (sent.result() == SendResult.SENT) {
                    first = sent;
                    photo = true;
                } else if (sent.result() == SendResult.TRANSIENT) {
                    // Временный сбой (фото/S3) — не теряем обложку, повторим весь пост.
                    return PublishResult.retry();
                } else {
                    log.info("sendPhoto отклонён Telegram для {} — отправляю текстом", article.getUrl());
                }
            }
        }
        if (first == null) {
            // Фото не отправляли (нет/отклонено) — текстом; лимит первого сообщения теперь 4096.
            messages = buildMessages(article, MESSAGE_LIMIT);
            first = send("sendMessage", messagePayload(messages.get(0)));
        }

        if (first.result() != SendResult.SENT) {
            return first.result() == SendResult.TRANSIENT ? PublishResult.retry() : PublishResult.giveUp();
        }

        List<Long> tailIds = sendTail(article, messages);
        return PublishResult.posted(first.messageId(), photo, tailIds);
    }

    /**
     * Досылает хвост многочастного поста отдельными сообщениями (best-effort: если кусок не ушёл, пост
     * неполный, но первое сообщение не дублируем). Возвращает id успешно отправленных хвостовых
     * сообщений — их сохраняем, чтобы при удалении новости убрать из канала ВЕСЬ пост.
     */
    private List<Long> sendTail(Article article, List<String> messages) {
        List<Long> ids = new ArrayList<>();
        for (int i = 1; i < messages.size(); i++) {
            SendOutcome outcome = send("sendMessage", messagePayload(messages.get(i)));
            if (outcome.result() == SendResult.SENT) {
                if (outcome.messageId() != null) {
                    ids.add(outcome.messageId());
                }
            } else {
                log.warn("Не отправлено хвостовое сообщение {}/{} для {} — пост неполный",
                        i, messages.size() - 1, article.getUrl());
            }
        }
        return ids;
    }

    /**
     * Синхронизирует пост с обновлённой новостью: правит caption (если пост с фото) или text ПЕРВОГО
     * сообщения на месте. Картинку в посте не трогаем — при смене обложки её меняют вручную. Хвостовые
     * сообщения многочастного поста правка тоже не трогает (правится только первое).
     */
    public EditResult editPost(Article article) {
        boolean photo = article.isTelegramPhoto();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("chat_id", properties.getChatId());
        payload.put("message_id", article.getTelegramMessageId());
        payload.put(photo ? "caption" : "text",
                buildMessages(article, photo ? CAPTION_LIMIT : MESSAGE_LIMIT).get(0));
        payload.put("parse_mode", properties.getParseMode());

        // Текстовый пост с обложкой-превью: пере-передаём link_preview_options, иначе editMessageText потеряет
        // картинку над текстом. Для поста без обложки (cover == null) ключ не добавляем — поведение прежнее.
        if (!photo) {
            String cover = coverUrl(article.getPreviewImageUrl());
            if (cover != null) {
                payload.put("link_preview_options", linkPreviewOptions(cover));
            }
        }

        return switch (send(photo ? "editMessageCaption" : "editMessageText", payload).result()) {
            case SENT -> EditResult.SYNCED;
            case TRANSIENT -> EditResult.RETRY;
            // 4xx: «message is not modified» (уже синхронно), пост удалён и т.п. — прекращаем попытки.
            case REJECTED -> EditResult.GIVE_UP;
        };
    }

    /**
     * Удаляет пост из канала (для удалённой с сайта новости) — целиком: первое сообщение (с картинкой)
     * плюс все хвостовые сообщения многочастного поста. Идемпотентно: уже удалённое сообщение Telegram
     * отвергает 4xx («message to delete not found») — для нас это «уже готово». Пока есть хоть один
     * временный сбой, возвращаем RETRY и повторяем ВЕСЬ проход на следующем тике (маркер снимет
     * планировщик лишь после не-RETRY, а повторное удаление уже убранных сообщений безопасно).
     */
    public EditResult deletePost(Article article) {
        List<Long> ids = new ArrayList<>();
        if (article.getTelegramMessageId() != null) {
            ids.add(article.getTelegramMessageId());
        }
        ids.addAll(parseTailIds(article.getTelegramTailMessageIds()));

        boolean anyTransient = false;
        for (Long id : ids) {
            // SENT (удалили) и REJECTED (сообщения уже нет / нельзя удалить) одинаково считаем обработанными;
            // повтор нужен только на временных сбоях (429/5xx/сеть).
            if (send("deleteMessage", deletePayload(id)).result() == SendResult.TRANSIENT) {
                anyTransient = true;
            }
        }
        return anyTransient ? EditResult.RETRY : EditResult.SYNCED;
    }

    /** Разбирает CSV хвостовых id сообщений; мусор/пустые значения пропускает, чтобы не ломать удаление. */
    private static List<Long> parseTailIds(String csv) {
        if (!StringUtils.hasText(csv)) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (String part : csv.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                try {
                    ids.add(Long.parseLong(trimmed));
                } catch (NumberFormatException ignored) {
                    // битое значение в CSV не должно мешать удалить остальные сообщения
                }
            }
        }
        return ids;
    }

    private Map<String, Object> deletePayload(Long messageId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("chat_id", properties.getChatId());
        payload.put("message_id", messageId);
        return payload;
    }

    /**
     * Собирает список сообщений: первое включает жирный заголовок и первый кусок описания (≤ firstLimit),
     * остальные — продолжение описания (≤ MESSAGE_LIMIT).
     */
    private List<String> buildMessages(Article article, int firstLimit) {
        String title = nullToEmpty(article.getTitle());
        String head = "<b>" + escape(title) + "</b>";
        int firstBodyLimit = Math.max(0, firstLimit - title.length() - 2);
        List<String> bodyChunks = formatter.toHtmlChunks(description(article), firstBodyLimit, MESSAGE_LIMIT);

        List<String> messages = new ArrayList<>();
        String firstBody = bodyChunks.isEmpty() ? "" : bodyChunks.get(0);
        messages.add(StringUtils.hasText(firstBody) ? head + "\n\n" + firstBody : head);
        for (int i = 1; i < bodyChunks.size(); i++) {
            if (StringUtils.hasText(bodyChunks.get(i))) {
                messages.add(bodyChunks.get(i));
            }
        }
        return messages;
    }

    /**
     * Пытается отправить первое сообщение с обложкой. Обложку из S3 заливаем файлом; для внешнего
     * абсолютного URL отдаём ссылку. {@code null} — обложки нет (уходим в текст).
     */
    private SendOutcome trySendPhoto(Article article, String caption) {
        String url = article.getPreviewImageUrl();
        if (!StringUtils.hasText(url)) {
            return null;
        }
        byte[] bytes = imageSource.bytes(url);
        if (bytes != null) {
            return sendPhotoBytes(bytes, imageSource.filename(url), caption);
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return send("sendPhoto", photoUrlPayload(url, caption));
        }
        return null;
    }

    private SendOutcome sendPhotoBytes(byte[] image, String filename, String caption) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("chat_id", properties.getChatId());
        if (StringUtils.hasText(caption)) {
            builder.part("caption", caption);
            builder.part("parse_mode", properties.getParseMode());
        }
        builder.part("photo", new ByteArrayResource(image) {
            @Override
            public String getFilename() {
                return filename;
            }
        });
        return sendBody("sendPhoto", builder.build(), MediaType.MULTIPART_FORM_DATA);
    }

    private Map<String, Object> photoUrlPayload(String url, String caption) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("chat_id", properties.getChatId());
        payload.put("photo", url);
        payload.put("caption", caption);
        payload.put("parse_mode", properties.getParseMode());
        return payload;
    }

    private Map<String, Object> messagePayload(String text) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("chat_id", properties.getChatId());
        payload.put("text", text);
        payload.put("parse_mode", properties.getParseMode());
        return payload;
    }

    /** Пейлоад текстового поста с обложкой-превью НАД текстом ({@code link_preview_options.show_above_text}). */
    private Map<String, Object> linkPreviewPayload(String text, String coverUrl) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("chat_id", properties.getChatId());
        payload.put("text", text);
        payload.put("parse_mode", properties.getParseMode());
        payload.put("link_preview_options", linkPreviewOptions(coverUrl));
        return payload;
    }

    /** {@code link_preview_options}: показать превью обложки крупным медиа и НАД текстом (Bot API 7.0+). */
    private Map<String, Object> linkPreviewOptions(String coverUrl) {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("url", coverUrl);
        options.put("prefer_large_media", true);
        options.put("show_above_text", true);
        return options;
    }

    /**
     * Абсолютный публичный URL обложки для превью-ссылки. {@code previewImageUrl} хранится относительным
     * ({@code /s3/<key>}), а Telegram для превью сам качает картинку по ссылке — значит нужен полный публичный
     * адрес ({@code app.url} + путь). Внешний абсолютный URL отдаём как есть. {@code null} — обложки нет либо
     * {@code app.url} не публичный (localhost/без схемы): тогда превью не применяем, уходим на фото с подписью.
     */
    private String coverUrl(String previewImageUrl) {
        if (!StringUtils.hasText(previewImageUrl)) {
            return null;
        }
        if (previewImageUrl.startsWith("http://") || previewImageUrl.startsWith("https://")) {
            return previewImageUrl;
        }
        // app.url — публичный адрес сайта, откуда отдаётся /s3/<key> (в проде https://new.ttg.club). Не-публичную
        // базу (localhost/без схемы) не используем — Telegram качает картинку по ссылке сам и до localhost не дойдёт.
        String base = appUrl == null ? "" : appUrl.trim();
        if (!(base.startsWith("http://") || base.startsWith("https://")) || base.contains("localhost")) {
            return null;
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return previewImageUrl.startsWith("/") ? base + previewImageUrl : base + "/" + previewImageUrl;
    }

    private SendOutcome send(String method, Map<String, Object> payload) {
        return sendBody(method, payload, MediaType.APPLICATION_JSON);
    }

    private SendOutcome sendBody(String method, Object body, MediaType contentType) {
        try {
            JsonNode response = telegramRestClient.post()
                    // Токен — часть пути (…/bot<token>/method). В шаблон не оборачиваем,
                    // чтобы ':' в токене не был percent-энкоднут.
                    .uri("/bot" + properties.getBotToken() + "/" + method)
                    .contentType(contentType)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            Long messageId = response != null ? response.path("result").path("message_id").asLong() : null;
            return new SendOutcome(SendResult.SENT, messageId);
        } catch (RestClientResponseException ex) {
            log.warn("Telegram {} вернул {}: {}", method, ex.getStatusCode(), ex.getResponseBodyAsString());
            boolean retriable = ex.getStatusCode().value() == 429 || ex.getStatusCode().is5xxServerError();
            return new SendOutcome(retriable ? SendResult.TRANSIENT : SendResult.REJECTED, null);
        } catch (RestClientException ex) {
            // Сеть/таймаут (в т.ч. потерянный ответ на уже доставленный запрос) — считаем временным.
            // Логируем причину (IOException), а НЕ ex.getMessage(): RestClient вшивает в него полный URL
            // запроса, а в пути — токен бота (…/bot<token>/…). Причина URL не содержит.
            Throwable cause = ex.getCause();
            log.warn("Не удалось вызвать Telegram {}: {}", method,
                    cause != null ? cause.getMessage() : ex.getClass().getSimpleName());
            return new SendOutcome(SendResult.TRANSIENT, null);
        }
    }

    /** Разметка описания: превью, если оно даёт непустой текст; иначе основной текст. */
    private String description(Article article) {
        return StringUtils.hasText(formatter.toPlain(article.getPreview()))
                ? article.getPreview()
                : article.getContent();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Итог вызова Bot API: результат и id сообщения (при успехе отправки). */
    private record SendOutcome(SendResult result, Long messageId) {
    }
}
