package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.config.properties.DiscordProperties;
import club.ttg.dnd5.domain.article.model.Article;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Отправляет и синхронизирует пост статьи / новости в Discord-канале через входящий вебхук.
 * <p>
 * Ничего не знает про БД и транзакции — только строит текст/пейлоад и делает HTTP-вызов. Пост — обычное
 * сообщение (не embed): жирный заголовок + текст в Discord-markdown (см. {@link DiscordMarkdownFormatter}),
 * обложку прикрепляем файлом из S3 (см. {@link ArticleImageSource}). Длинный пост не обрезается: первый кусок
 * (≤2000) идёт первым сообщением с обложкой, остаток досылается отдельными сообщениями (≤2000).
 * <p>
 * Отправляем с {@code ?wait=true}, чтобы получить id сообщения (снежинку) — он нужен для правки
 * ({@code PATCH /messages/{id}}) и удаления ({@code DELETE /messages/{id}}). Итог отправки — явный статус,
 * чтобы планировщик отличал «повторить» (временный сбой) от «сдаться» (перманентный отказ Discord).
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DiscordPublisher {

    /** Лимит длины сообщения Discord (символы; считается по сырому тексту, включая markdown). */
    private static final int MESSAGE_LIMIT = 2000;

    /** Итог одного вызова вебхука. */
    private enum SendResult {
        /** 2xx — отправлено. */
        SENT,
        /** 4xx (кроме 429) — запрос неверен навсегда (битая картинка/чат/сообщение удалено): повтор бесполезен. */
        REJECTED,
        /** 429/5xx/сеть/таймаут — временно: имеет смысл повторить. */
        TRANSIENT
    }

    /** Итог отправки нового поста (messageId — id ПЕРВОГО сообщения). */
    public record PublishResult(Status status, String messageId) {
        public enum Status { POSTED, RETRY, GIVE_UP }

        static PublishResult posted(String messageId) {
            return new PublishResult(Status.POSTED, messageId);
        }

        static PublishResult retry() {
            return new PublishResult(Status.RETRY, null);
        }

        static PublishResult giveUp() {
            return new PublishResult(Status.GIVE_UP, null);
        }
    }

    /** Итог синхронизации правки / удаления поста. */
    public enum EditResult { SYNCED, RETRY, GIVE_UP }

    private final RestClient discordRestClient;
    private final DiscordProperties properties;
    private final DiscordMarkdownFormatter formatter;
    private final ArticleImageSource imageSource;
    private final ObjectMapper objectMapper;

    /**
     * Отправляет новый пост в канал (при необходимости — несколькими сообщениями).
     */
    public PublishResult publish(Article article) {
        if (!StringUtils.hasText(article.getTitle()) && !StringUtils.hasText(formatter.toPlain(description(article)))) {
            log.warn("Пустой заголовок и текст — пропускаю отправку {} в Discord", article.getUrl());
            return PublishResult.giveUp();
        }

        List<String> messages = buildMessages(article);
        if (messages.isEmpty()) {
            // Ни заголовка, ни текста после рендера — постить нечего (страховка; обычно отсекается проверкой выше).
            return PublishResult.giveUp();
        }

        // Первое сообщение: с обложкой (файлом), если она есть; иначе текстом.
        SendOutcome first = null;
        if (StringUtils.hasText(article.getPreviewImageUrl())) {
            SendOutcome sent = trySendPhoto(article, messages.get(0));
            if (sent != null) {
                if (sent.result() == SendResult.TRANSIENT) {
                    // Временный сбой (загрузка файла/сеть) — не теряем обложку, повторим весь пост.
                    return PublishResult.retry();
                }
                if (sent.result() == SendResult.SENT) {
                    first = sent;
                } else {
                    log.info("Отправка обложки отклонена Discord для {} — отправляю текстом", article.getUrl());
                }
            }
        }
        if (first == null) {
            first = sendMessage(messages.get(0));
        }

        if (first.result() != SendResult.SENT) {
            return first.result() == SendResult.TRANSIENT ? PublishResult.retry() : PublishResult.giveUp();
        }
        String firstId = first.messageId();

        // Хвост — отдельными сообщениями (best-effort: если не ушло, пост неполный, но первое не дублируем).
        for (int i = 1; i < messages.size(); i++) {
            if (sendMessage(messages.get(i)).result() != SendResult.SENT) {
                log.warn("Не отправлено хвостовое сообщение {}/{} для {} — пост неполный",
                        i, messages.size() - 1, article.getUrl());
            }
        }
        return PublishResult.posted(firstId);
    }

    /**
     * Синхронизирует пост с обновлённой новостью: правит content ПЕРВОГО сообщения на месте. Вложение
     * (обложку) не трогаем — поле {@code attachments} не передаём, и Discord сохраняет уже загруженный файл.
     * Хвостовые сообщения многочастного поста правка тоже не трогает (правится только первое).
     */
    public EditResult editPost(Article article) {
        List<String> messages = buildMessages(article);
        if (messages.isEmpty()) {
            // Правка сделала запись пустой — пустой content Discord отвергнет, корректной альтернативы нет:
            // прекращаем попытки (планировщик снимет флаг dirty).
            return EditResult.GIVE_UP;
        }
        Map<String, Object> payload = messagePayload(messages.get(0));
        return switch (send(HttpMethod.PATCH, messageUri(article.getDiscordMessageId()), payload,
                MediaType.APPLICATION_JSON).result()) {
            case SENT -> EditResult.SYNCED;
            case TRANSIENT -> EditResult.RETRY;
            // 4xx: сообщение удалено/нельзя изменить — прекращаем попытки.
            case REJECTED -> EditResult.GIVE_UP;
        };
    }

    /** Удаляет пост из канала (для удалённой с сайта новости). */
    public EditResult deletePost(Article article) {
        return switch (send(HttpMethod.DELETE, messageUri(article.getDiscordMessageId()), null, null).result()) {
            case SENT -> EditResult.SYNCED;
            case TRANSIENT -> EditResult.RETRY;
            // «Unknown Message» / нельзя удалить — считаем обработанным (больше не пытаемся).
            case REJECTED -> EditResult.GIVE_UP;
        };
    }

    /**
     * Собирает список сообщений: первое включает жирный заголовок и первый кусок описания (≤ MESSAGE_LIMIT),
     * остальные — продолжение описания (≤ MESSAGE_LIMIT).
     */
    private List<String> buildMessages(Article article) {
        String title = nullToEmpty(article.getTitle());
        // Заголовок — литеральный текст: экранируем markdown-метасимволы (в т.ч. `[`/`]`, чтобы `[текст](url)`
        // в названии не стал ссылкой), чтобы разметка в названии не ломала жирную «шапку» поста (тело,
        // наоборот, несёт намеренную разметку и не экранируется).
        String head = StringUtils.hasText(title) ? "**" + escapeMarkdown(title) + "**" : "";
        int firstBodyLimit = Math.max(0, MESSAGE_LIMIT - head.length() - 2);
        List<String> bodyChunks = formatter.toMarkdownChunks(description(article), firstBodyLimit, MESSAGE_LIMIT);

        List<String> messages = new ArrayList<>();
        String firstBody = bodyChunks.isEmpty() ? "" : bodyChunks.get(0);
        String first = StringUtils.hasText(firstBody)
                ? (head.isEmpty() ? firstBody : head + "\n\n" + firstBody)
                : head;
        // Пустое первое сообщение не шлём (Discord отвергнет content=""): если заголовок пуст и первый
        // кусок описания «отложен» (пустой), пост начнётся с первого непустого куска.
        if (StringUtils.hasText(first)) {
            messages.add(first);
        }
        for (int i = 1; i < bodyChunks.size(); i++) {
            if (StringUtils.hasText(bodyChunks.get(i))) {
                messages.add(bodyChunks.get(i));
            }
        }
        return messages;
    }

    /**
     * Пытается отправить первое сообщение с обложкой файлом. {@code null} — обложки нет (это не S3-путь
     * или объекта нет) — тогда уходим текстом. Временный сбой чтения из S3 пробрасывается наружу
     * (планировщик освободит запись и повторит).
     */
    private SendOutcome trySendPhoto(Article article, String content) {
        byte[] bytes = imageSource.bytes(article.getPreviewImageUrl());
        if (bytes == null) {
            return null;
        }
        return sendMessageWithImage(content, bytes, imageSource.filename(article.getPreviewImageUrl()));
    }

    private SendOutcome sendMessageWithImage(String content, byte[] image, String filename) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        // Текст и настройки — JSON-частью payload_json, картинка — отдельным файлом (files[0]).
        builder.part("payload_json", toJson(messagePayload(content)), MediaType.APPLICATION_JSON);
        builder.part("files[0]", new ByteArrayResource(image) {
            @Override
            public String getFilename() {
                return filename;
            }
        });
        return send(HttpMethod.POST, sendUri(), builder.build(), MediaType.MULTIPART_FORM_DATA);
    }

    private SendOutcome sendMessage(String content) {
        return send(HttpMethod.POST, sendUri(), messagePayload(content), MediaType.APPLICATION_JSON);
    }

    private Map<String, Object> messagePayload(String content) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("content", content);
        // Не пинговать @everyone/@here/роли, если они случайно окажутся в тексте новости.
        payload.put("allowed_mentions", Map.of("parse", List.of()));
        return payload;
    }

    private SendOutcome send(HttpMethod method, URI uri, Object body, MediaType contentType) {
        try {
            RestClient.RequestBodySpec request = discordRestClient.method(method).uri(uri);
            RestClient.ResponseSpec response = body != null
                    ? request.contentType(contentType).body(body).retrieve()
                    : request.retrieve();
            JsonNode json = response.body(JsonNode.class);
            // Discord возвращает id сообщения строкой (снежинка) — на POST ?wait=true и на PATCH.
            String messageId = json != null ? json.path("id").asText(null) : null;
            return new SendOutcome(SendResult.SENT, messageId);
        } catch (RestClientResponseException ex) {
            log.warn("Discord {} вернул {}: {}", method, ex.getStatusCode(), ex.getResponseBodyAsString());
            boolean retriable = ex.getStatusCode().value() == 429 || ex.getStatusCode().is5xxServerError();
            return new SendOutcome(retriable ? SendResult.TRANSIENT : SendResult.REJECTED, null);
        } catch (RestClientException ex) {
            // Сеть/таймаут (в т.ч. потерянный ответ на уже доставленный запрос) — считаем временным.
            // Логируем причину (IOException), а НЕ ex.getMessage(): RestClient вшивает в него полный URL
            // запроса, а весь URL вебхука (вместе с токеном) — это целиком секрет доступа к каналу.
            Throwable cause = ex.getCause();
            log.warn("Не удалось вызвать Discord {}: {}", method,
                    cause != null ? cause.getMessage() : ex.getClass().getSimpleName());
            return new SendOutcome(SendResult.TRANSIENT, null);
        }
    }

    /** Адрес отправки нового сообщения: сам вебхук с {@code ?wait=true} (чтобы вернулся id сообщения). */
    private URI sendUri() {
        return URI.create(properties.getWebhookUrl() + "?wait=true");
    }

    /** Адрес правки/удаления конкретного сообщения вебхука. */
    private URI messageUri(String messageId) {
        return URI.create(properties.getWebhookUrl() + "/messages/" + messageId);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Не удалось сериализовать пейлоад Discord", ex);
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

    /**
     * Экранирует markdown-метасимволы Discord обратным слэшем — для литерального текста (заголовок),
     * который не должен интерпретироваться как разметка. Аналог {@code escape()} в TelegramPublisher.
     */
    private static String escapeMarkdown(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '*' || c == '_' || c == '~' || c == '|' || c == '`' || c == '>'
                    || c == '[' || c == ']') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /** Итог вызова вебхука: результат и id сообщения (при успехе отправки). */
    private record SendOutcome(SendResult result, String messageId) {
    }
}
