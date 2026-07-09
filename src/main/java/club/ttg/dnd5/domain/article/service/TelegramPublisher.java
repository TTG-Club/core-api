package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.config.properties.TelegramProperties;
import club.ttg.dnd5.domain.article.model.Article;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Отправляет и синхронизирует пост статьи / новости в Telegram-канале через Bot API.
 * <p>
 * Ничего не знает про БД и транзакции — только строит текст/пейлоад и делает HTTP-вызов. Длинный пост не
 * обрезается: первый кусок идёт в подпись к фото (≤1024) или в первое сообщение (≤4096), остальное
 * досылается отдельными сообщениями (≤4096). Текст поста — Telegram-HTML (см. {@link TelegramHtmlFormatter}),
 * обложку заливаем файлом из S3 (см. {@link ArticleImageSource}). Итог отправки — явный статус, чтобы
 * планировщик отличал «повторить» (временный сбой) от «сдаться» (перманентный отказ Telegram).
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

    /** Итог отправки нового поста (messageId — id ПЕРВОГО сообщения: фото/основного). */
    public record PublishResult(Status status, Long messageId, boolean photo) {
        public enum Status { POSTED, RETRY, GIVE_UP }

        static PublishResult posted(Long messageId, boolean photo) {
            return new PublishResult(Status.POSTED, messageId, photo);
        }

        static PublishResult retry() {
            return new PublishResult(Status.RETRY, null, false);
        }

        static PublishResult giveUp() {
            return new PublishResult(Status.GIVE_UP, null, false);
        }
    }

    /** Итог синхронизации правки поста. */
    public enum EditResult { SYNCED, RETRY, GIVE_UP }

    private final RestClient telegramRestClient;
    private final TelegramProperties properties;
    private final TelegramHtmlFormatter formatter;
    private final ArticleImageSource imageSource;

    /**
     * Отправляет новый пост в канал (при необходимости — несколькими сообщениями).
     */
    public PublishResult publish(Article article) {
        if (!StringUtils.hasText(article.getTitle()) && !StringUtils.hasText(formatter.toPlain(description(article)))) {
            log.warn("Пустой заголовок и текст — пропускаю отправку {} в Telegram", article.getUrl());
            return PublishResult.giveUp();
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
        Long firstId = first.messageId();

        // Хвост — отдельными сообщениями (best-effort: если не ушло, пост неполный, но не дублируем первое).
        for (int i = 1; i < messages.size(); i++) {
            if (send("sendMessage", messagePayload(messages.get(i))).result() != SendResult.SENT) {
                log.warn("Не отправлено хвостовое сообщение {}/{} для {} — пост неполный",
                        i, messages.size() - 1, article.getUrl());
            }
        }
        return PublishResult.posted(firstId, photo);
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

        return switch (send(photo ? "editMessageCaption" : "editMessageText", payload).result()) {
            case SENT -> EditResult.SYNCED;
            case TRANSIENT -> EditResult.RETRY;
            // 4xx: «message is not modified» (уже синхронно), пост удалён и т.п. — прекращаем попытки.
            case REJECTED -> EditResult.GIVE_UP;
        };
    }

    /** Удаляет пост из канала (для удалённой с сайта новости). */
    public EditResult deletePost(Article article) {
        return switch (send("deleteMessage", deletePayload(article.getTelegramMessageId())).result()) {
            case SENT -> EditResult.SYNCED;
            case TRANSIENT -> EditResult.RETRY;
            // «message to delete not found» / нельзя удалить — считаем обработанным (больше не пытаемся).
            case REJECTED -> EditResult.GIVE_UP;
        };
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
