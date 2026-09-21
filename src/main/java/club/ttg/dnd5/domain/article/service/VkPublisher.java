package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.config.properties.VkProperties;
import club.ttg.dnd5.domain.article.model.Article;
import club.ttg.dnd5.domain.image.service.ImageConverter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * Отправляет и синхронизирует пост статьи / новости на стене сообщества ВКонтакте — через ключ доступа
 * сообщества («API-ключ группы»), без бота.
 * <p>
 * Ничего не знает про БД и транзакции — только строит текст и делает HTTP-вызовы. Пост — обычный текст
 * (VK не рендерит форматирование, см. {@link VkTextFormatter}): заголовок + текст, обложку заливаем на
 * стену файлом из S3 (см. {@link ArticleImageSource}) — предварительно перекодировав её из WebP в JPEG/PNG,
 * потому что upload-сервер VK WebP не принимает. В отличие от Telegram/Discord пост один (стена — это
 * лента, дробить новость на несколько записей неуместно): очень длинный текст усекаем до лимита VK.
 * <p>
 * Пост уходит от имени сообщества ({@code wall.post}, {@code owner_id=-<groupId>}, {@code from_group=1});
 * возвращённый {@code post_id} нужен для правки ({@code wall.edit}) и удаления ({@code wall.delete}). Оба метода
 * VK разрешает только пользовательскому токену админа: ключу сообщества он отвечает ошибкой 27, и планировщик
 * просто перестаёт пытаться (править/удалять пост тогда приходится руками в ВК).
 * <p>
 * Особенность VK: сервер отвечает HTTP 200 даже на ошибку — она лежит в теле в {@code error.error_code}.
 * Часть кодов временные (повторить), часть — перманентные (сдаться), чтобы планировщик не долбил стену.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class VkPublisher {

    /** Практический лимит длины текста поста на стене VK (символы). Реальный предел ~16384 — берём с запасом. */
    private static final int MESSAGE_LIMIT = 16000;

    /** Качество JPEG при перекодировании обложки из WebP под требования ВК (см. {@link #toVkFormat}). */
    private static final float COVER_JPEG_QUALITY = 0.9f;

    /** Коды ошибок VK, при которых имеет смысл повторить (временные). Остальное — перманентный отказ. */
    private static final Set<Integer> TRANSIENT_ERROR_CODES = Set.of(
            1,  // Unknown error occurred — VK рекомендует повторить позже
            6,  // Too many requests per second
            9,  // Flood control
            10, // Internal server error
            14  // Captcha needed (для серверного постинга редкость; повторим позже)
    );

    /** Итог одного вызова VK API. */
    private enum SendResult {
        /** Успех (в теле есть {@code response} и нет {@code error}). */
        SENT,
        /** Перманентный отказ VK (битые параметры / нет доступа / неверный токен) — повтор бесполезен. */
        REJECTED,
        /** Временный сбой (retryable error_code / 429 / 5xx / сеть / таймаут) — имеет смысл повторить. */
        TRANSIENT
    }

    /** Итог отправки нового поста. */
    public record PublishResult(Status status, Long postId, String attachment) {
        public enum Status { POSTED, RETRY, GIVE_UP }

        static PublishResult posted(Long postId, String attachment) {
            return new PublishResult(Status.POSTED, postId, attachment);
        }

        static PublishResult retry() {
            return new PublishResult(Status.RETRY, null, null);
        }

        static PublishResult giveUp() {
            return new PublishResult(Status.GIVE_UP, null, null);
        }
    }

    /** Итог синхронизации правки / удаления поста. */
    public enum EditResult { SYNCED, RETRY, GIVE_UP }

    /**
     * Итог синхронизации правки поста: статус + строка вложения-обложки, если её залили прямо во время правки
     * ({@code null} — обложку не трогали). Ненулевой {@code newAttachment} планировщик сохраняет в БД, чтобы
     * следующие правки её пере-передавали и не заливали фото заново.
     */
    public record EditOutcome(EditResult status, String newAttachment) {
        static EditOutcome of(EditResult status) {
            return new EditOutcome(status, null);
        }
    }

    private final RestClient vkRestClient;
    private final VkProperties properties;
    private final VkTextFormatter formatter;
    private final ArticleImageSource imageSource;

    /**
     * Отправляет новый пост на стену сообщества (одной записью). Обложка — best-effort: если её не удалось
     * залить по перманентной причине (напр. VK не принял картинку), пост уходит текстом.
     */
    public PublishResult publish(Article article) {
        String message = buildMessage(article);
        if (!StringUtils.hasText(message)) {
            log.warn("Пустой заголовок и текст — пропускаю отправку {} в VK", article.getUrl());
            return PublishResult.giveUp();
        }

        Cover cover = uploadCover(article);
        if (cover.retryNeeded()) {
            // Временный сбой загрузки обложки/чтения из S3 — не теряем обложку, повторим весь пост.
            return PublishResult.retry();
        }
        String attachment = cover.attachment();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("owner_id", ownerId());
        params.add("from_group", "1");
        params.add("message", message);
        if (attachment != null) {
            params.add("attachments", attachment);
        }
        ApiOutcome outcome = callMethod("wall.post", params);
        return switch (outcome.result()) {
            case SENT -> {
                Long postId = postId(outcome.response());
                if (postId == null) {
                    // Успех без post_id (не должно случаться) — пост опубликован, но править/удалять его
                    // мы уже не сможем. Помечаем отправленным (без id), чтобы не запостить повторно.
                    log.warn("VK не вернул post_id для {} — правка/удаление поста будут недоступны", article.getUrl());
                }
                yield PublishResult.posted(postId, attachment);
            }
            case TRANSIENT -> PublishResult.retry();
            case REJECTED -> PublishResult.giveUp();
        };
    }

    /**
     * Синхронизирует пост с обновлённой новостью: правит текст на месте ({@code wall.edit}). Сохранённую
     * строку вложения (обложку) пере-передаём, потому что {@code wall.edit} без {@code attachments} может
     * очистить вложения — так фото остаётся на месте.
     * <p>
     * Если сохранённой обложки нет ({@code vkAttachment} пуст), а у новости картинка теперь есть — заливаем
     * её прямо сейчас и до-прикрепляем. Это закрывает случаи, когда пост изначально ушёл текстом: обложку
     * добавили уже после первой публикации, либо первая публикация не смогла её залить (напр. позже сменили
     * ключ сообщества на пользовательский токен админа). Залитую строку вложения возвращаем в
     * {@link EditOutcome#newAttachment()} — планировщик сохранит её, чтобы не заливать фото на каждой правке.
     */
    public EditOutcome editPost(Article article) {
        String message = buildMessage(article);
        if (!StringUtils.hasText(message)) {
            // Правка сделала запись пустой — корректной альтернативы нет: прекращаем попытки (флаг снимет поллер).
            return EditOutcome.of(EditResult.GIVE_UP);
        }

        String attachment = article.getVkAttachment();
        String uploadedAttachment = null;
        if (!StringUtils.hasText(attachment)) {
            Cover cover = uploadCover(article);
            if (cover.retryNeeded()) {
                // Временный сбой загрузки обложки — правку отложим, повторим весь проход на следующем тике.
                return EditOutcome.of(EditResult.RETRY);
            }
            // null — обложки нет либо VK её не принял: правим текст без фото.
            attachment = cover.attachment();
            uploadedAttachment = cover.attachment();
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("owner_id", ownerId());
        params.add("post_id", String.valueOf(article.getVkPostId()));
        params.add("message", message);
        if (StringUtils.hasText(attachment)) {
            params.add("attachments", attachment);
        }
        EditResult status = switch (callMethod("wall.edit", params).result()) {
            case SENT -> EditResult.SYNCED;
            case TRANSIENT -> EditResult.RETRY;
            // Пост удалён/нельзя изменить — прекращаем попытки.
            case REJECTED -> EditResult.GIVE_UP;
        };
        // Свежезалитую обложку сохраняем в любом исходе (даже при RETRY): фото уже в альбоме сообщества,
        // повторная правка пере-прикрепит его по сохранённой строке, а не зальёт файл заново.
        return new EditOutcome(status, uploadedAttachment);
    }

    /** Удаляет пост со стены (для удалённой с сайта новости). */
    public EditResult deletePost(Article article) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("owner_id", ownerId());
        params.add("post_id", String.valueOf(article.getVkPostId()));
        return switch (callMethod("wall.delete", params).result()) {
            case SENT -> EditResult.SYNCED;
            case TRANSIENT -> EditResult.RETRY;
            // Поста уже нет / нельзя удалить — считаем обработанным (больше не пытаемся).
            case REJECTED -> EditResult.GIVE_UP;
        };
    }

    /** Собирает текст поста: заголовок + текст описания, усечённые до лимита VK. */
    private String buildMessage(Article article) {
        String title = article.getTitle() == null ? "" : article.getTitle().strip();
        String body = formatter.toText(description(article));
        String combined;
        if (StringUtils.hasText(title)) {
            combined = StringUtils.hasText(body) ? title + "\n\n" + body : title;
        } else {
            combined = body;
        }
        return truncate(combined, MESSAGE_LIMIT);
    }

    /**
     * Заливает обложку в альбом сообщества и возвращает строку вложения для поста. Сначала штатным путём —
     * на стену ({@link CoverRoute#WALL}); если VK не выдал upload-сервер стены (ключу сообщества он отвечает
     * ошибкой 27), — через альбом сообщений ({@link CoverRoute#MESSAGES}). Best-effort: нет картинки или
     * перманентный отказ VK → {@link Cover#none()} (пост уйдёт текстом); временный сбой →
     * {@link Cover#retry()} (повторить весь пост).
     */
    private Cover uploadCover(Article article) {
        if (!StringUtils.hasText(article.getPreviewImageUrl())) {
            // Единственная ветка без лога прятала главный сценарий бага: запись создана и опубликована
            // раньше, чем обложка доехала до БД (гонка на фронте) — пост молча уходил текстом.
            log.info("У записи {} нет обложки в момент отправки — отправляю текстом", article.getUrl());
            return Cover.none();
        }
        // Байты обложки из S3: null — картинки нет (не S3-путь, напр. внешний URL, или объекта нет в бакете);
        // временный сбой чтения пробрасывается наружу (планировщик освободит запись и повторит).
        byte[] bytes = imageSource.bytes(article.getPreviewImageUrl());
        if (bytes == null) {
            log.info("Не удалось прочитать байты обложки {} для {} — отправляю текстом",
                    article.getPreviewImageUrl(), article.getUrl());
            return Cover.none();
        }

        // Обложки лежат в S3 в WebP (см. ImageService#upload), а upload-сервер ВК принимает только JPG/PNG/GIF
        // и на WebP молча отвечает photo="[]" — поэтому перекодируем перед загрузкой.
        ImageConverter.EncodedImage encoded = toVkFormat(bytes, article);
        String filename = coverFilename(imageSource.filename(article.getPreviewImageUrl()), encoded.extension());
        Cover cover = uploadCover(article, encoded, filename, CoverRoute.WALL);
        if (cover == null) {
            log.info("VK не дал загрузить обложку на стену для {} — пробую через альбом сообщений", article.getUrl());
            cover = uploadCover(article, encoded, filename, CoverRoute.MESSAGES);
        }
        if (cover == null) {
            log.info("VK отказал в загрузке обложки для {} — отправляю текстом", article.getUrl());
            return Cover.none();
        }
        return cover;
    }

    /**
     * Заливает обложку одним путём VK (3 шага: выдать upload-сервер → загрузить файл → сохранить фото).
     * {@code null} — VK не выдал upload-сервер (у ключа нет доступа к этому пути), можно пробовать другой.
     */
    private Cover uploadCover(Article article, ImageConverter.EncodedImage encoded, String filename,
                              CoverRoute route) {
        ApiOutcome server = callMethod(route.serverMethod, route.groupParams(properties.getGroupId()));
        if (server.result() == SendResult.TRANSIENT) {
            return Cover.retry();
        }
        if (server.result() == SendResult.REJECTED) {
            return null;
        }
        String uploadUrl = server.response() != null ? server.response().path("upload_url").asText(null) : null;
        if (!StringUtils.hasText(uploadUrl)) {
            return Cover.none();
        }

        ApiOutcome uploaded = uploadFile(uploadUrl, encoded, filename);
        if (uploaded.result() == SendResult.TRANSIENT) {
            return Cover.retry();
        }
        if (uploaded.result() == SendResult.REJECTED || uploaded.response() == null) {
            log.info("VK отклонил файл обложки при загрузке для {} — отправляю текстом", article.getUrl());
            return Cover.none();
        }
        String photo = uploaded.response().path("photo").asText("");
        // Пустой photo (или "[]") — VK не принял файл. Формат мы уже привели к JPEG/PNG, так что остаются
        // размер/пропорции картинки, которые ВК фильтрует строже Telegram/Discord: постим без обложки.
        if (!StringUtils.hasText(photo) || "[]".equals(photo)) {
            log.warn("VK не принял картинку обложки {} для {} (размер/пропорции?) — отправляю текстом",
                    article.getPreviewImageUrl(), article.getUrl());
            return Cover.none();
        }

        MultiValueMap<String, String> saveParams = route.groupParams(properties.getGroupId());
        saveParams.add("server", String.valueOf(uploaded.response().path("server").asInt()));
        saveParams.add("photo", photo);
        saveParams.add("hash", uploaded.response().path("hash").asText(""));
        ApiOutcome saved = callMethod(route.saveMethod, saveParams);
        if (saved.result() == SendResult.TRANSIENT) {
            return Cover.retry();
        }
        JsonNode arr = saved.response();
        if (saved.result() == SendResult.REJECTED || arr == null || !arr.isArray() || arr.isEmpty()) {
            log.info("VK не сохранил обложку ({}) для {} — отправляю текстом", route.saveMethod, article.getUrl());
            return Cover.none();
        }
        return Cover.of(photoAttachment(arr.get(0)));
    }

    /**
     * Строка вложения {@code photo<owner_id>_<id>[_<access_key>]}. owner_id группы уже отрицательный; ключ
     * доступа VK отдаёт для фото из закрытых альбомов (альбом сообщений) — передаём его, если он есть.
     */
    private static String photoAttachment(JsonNode photo) {
        String attachment = "photo" + photo.path("owner_id").asLong() + "_" + photo.path("id").asLong();
        String accessKey = photo.path("access_key").asText("");
        return StringUtils.hasText(accessKey) ? attachment + "_" + accessKey : attachment;
    }

    /** Вызов метода VK API: POST form-urlencoded, добавляет {@code access_token} и {@code v}, разбирает ошибку. */
    private ApiOutcome callMethod(String method, MultiValueMap<String, String> params) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>(params);
        form.add("access_token", properties.getAccessToken());
        form.add("v", properties.getApiVersion());
        URI uri = URI.create(properties.getApiUrl() + "/" + method);
        try {
            JsonNode json = vkRestClient.post().uri(uri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);
            if (json != null && json.has("error")) {
                JsonNode error = json.get("error");
                int code = error.path("error_code").asInt();
                log.warn("VK {} вернул ошибку {}: {}", method, code, error.path("error_msg").asText());
                return new ApiOutcome(
                        TRANSIENT_ERROR_CODES.contains(code) ? SendResult.TRANSIENT : SendResult.REJECTED, null);
            }
            return new ApiOutcome(SendResult.SENT, json != null ? json.get("response") : null);
        } catch (RestClientResponseException ex) {
            log.warn("VK {} HTTP {}: {}", method, ex.getStatusCode(), ex.getResponseBodyAsString());
            boolean retriable = ex.getStatusCode().value() == 429 || ex.getStatusCode().is5xxServerError();
            return new ApiOutcome(retriable ? SendResult.TRANSIENT : SendResult.REJECTED, null);
        } catch (RestClientException ex) {
            // Сеть/таймаут — временный сбой. Логируем причину, а не ex.getMessage() (в него RestClient
            // вшивает URL запроса — на всякий случай не тащим его в лог).
            Throwable cause = ex.getCause();
            log.warn("Не удалось вызвать VK {}: {}", method,
                    cause != null ? cause.getMessage() : ex.getClass().getSimpleName());
            return new ApiOutcome(SendResult.TRANSIENT, null);
        }
    }

    /**
     * Перекодирует обложку в формат, который принимает ВК (JPEG / PNG). Если перекодировать не вышло
     * (битый файл / нет ImageIO-плагина) — отдаём исходные байты: хуже, чем сейчас, не станет, а VK
     * ответит «не принял картинку» и пост уйдёт текстом.
     */
    private ImageConverter.EncodedImage toVkFormat(byte[] bytes, Article article) {
        try {
            return ImageConverter.toJpegOrPng(bytes, COVER_JPEG_QUALITY);
        } catch (IOException | RuntimeException ex) {
            log.warn("Не удалось перекодировать обложку {} для VK ({}) — пробую загрузить как есть",
                    article.getPreviewImageUrl(), ex.getMessage());
            return new ImageConverter.EncodedImage(bytes, MediaType.APPLICATION_OCTET_STREAM_VALUE, null);
        }
    }

    /** Имя файла обложки с расширением под фактический формат загружаемых байт ({@code cover.jpg}). */
    private static String coverFilename(String filename, String extension) {
        if (!StringUtils.hasText(extension)) {
            return filename;
        }
        int dot = filename.lastIndexOf('.');
        return (dot > 0 ? filename.substring(0, dot) : filename) + "." + extension;
    }

    /** Загрузка файла обложки на выданный VK upload-сервер (multipart, поле {@code photo}). */
    private ApiOutcome uploadFile(String uploadUrl, ImageConverter.EncodedImage image, String filename) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("photo", new ByteArrayResource(image.bytes()) {
            @Override
            public String getFilename() {
                return filename;
            }
        }).contentType(MediaType.parseMediaType(image.contentType()));
        try {
            JsonNode json = vkRestClient.post().uri(URI.create(uploadUrl))
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(builder.build())
                    .retrieve()
                    .body(JsonNode.class);
            return new ApiOutcome(SendResult.SENT, json);
        } catch (RestClientResponseException ex) {
            log.warn("VK upload обложки HTTP {}", ex.getStatusCode());
            boolean retriable = ex.getStatusCode().value() == 429 || ex.getStatusCode().is5xxServerError();
            return new ApiOutcome(retriable ? SendResult.TRANSIENT : SendResult.REJECTED, null);
        } catch (RestClientException ex) {
            Throwable cause = ex.getCause();
            log.warn("Не удалось загрузить обложку в VK: {}",
                    cause != null ? cause.getMessage() : ex.getClass().getSimpleName());
            return new ApiOutcome(SendResult.TRANSIENT, null);
        }
    }

    /** {@code post_id} из ответа wall.post; {@code null}, если его нет (или 0 — недопустимый id). */
    private static Long postId(JsonNode response) {
        if (response == null) {
            return null;
        }
        long id = response.path("post_id").asLong(0);
        return id == 0 ? null : id;
    }

    private String ownerId() {
        return "-" + properties.getGroupId();
    }

    /** Разметка описания: превью, если оно даёт непустой текст; иначе основной текст. */
    private String description(Article article) {
        return StringUtils.hasText(formatter.toText(article.getPreview()))
                ? article.getPreview()
                : article.getContent();
    }

    /** Усекает текст до {@code limit} символов по границе слова (плюс «…»), не разрывая суррогатную пару. */
    private static String truncate(String text, int limit) {
        if (text.length() <= limit) {
            return text;
        }
        int cut = text.lastIndexOf(' ', limit - 1);
        if (cut <= 0) {
            cut = limit - 1;
        }
        if (Character.isHighSurrogate(text.charAt(cut - 1))) {
            cut--;
        }
        return text.substring(0, cut).stripTrailing() + "…";
    }

    /** Итог загрузки обложки: {@code retryNeeded} — повторить весь пост; иначе строка вложения ({@code null} — без обложки). */
    private record Cover(boolean retryNeeded, String attachment) {
        static Cover retry() {
            return new Cover(true, null);
        }

        static Cover none() {
            return new Cover(false, null);
        }

        static Cover of(String attachment) {
            return new Cover(false, attachment);
        }
    }

    /** Путь заливки обложки: метод «выдать upload-сервер» и метод «сохранить загруженное фото». */
    private enum CoverRoute {
        /** Альбом стены сообщества — штатный путь; ключ сообщества VK сюда не пускает (ошибка 27). */
        WALL("photos.getWallUploadServer", "photos.saveWallPhoto", true),
        /**
         * Альбом сообщений сообщества — обход для ключа сообщества: фото всё равно принадлежит сообществу
         * ({@code owner_id=-<groupId>}) и прикрепляется к посту на стене.
         */
        MESSAGES("photos.getMessagesUploadServer", "photos.saveMessagesPhoto", false);

        private final String serverMethod;
        private final String saveMethod;
        /** Нужно ли передавать {@code group_id} (методам стены — да; методы сообщений берут сообщество из ключа). */
        private final boolean withGroupId;

        CoverRoute(String serverMethod, String saveMethod, boolean withGroupId) {
            this.serverMethod = serverMethod;
            this.saveMethod = saveMethod;
            this.withGroupId = withGroupId;
        }

        MultiValueMap<String, String> groupParams(String groupId) {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            if (withGroupId) {
                params.add("group_id", groupId);
            }
            return params;
        }
    }

    /** Итог вызова VK API: результат и полезная нагрузка ({@code response} для методов, тело — для загрузки). */
    private record ApiOutcome(SendResult result, JsonNode response) {
    }
}
