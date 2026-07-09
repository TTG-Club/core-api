package club.ttg.dnd5.domain.article.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * Достаёт байты обложки новости из S3, чтобы залить их в Telegram файлом (а не ссылкой).
 * previewImageUrl хранится как относительный путь {@code /s3/<key>}; публичного URL у объекта нет
 * (bucket закрыт), поэтому картинку читаем напрямую из S3 — доступ у сервиса уже есть.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TelegramImageSource {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Client s3Client;

    /**
     * Байты обложки по {@code previewImageUrl} вида {@code /s3/<key>}.
     * <p>
     * Различает «нет картинки» и «временный сбой»: {@code null} — это не S3-путь ИЛИ объекта нет
     * (NoSuchKey) — тогда пост уйдёт текстом. Временный сбой чтения (таймаут/5xx/сеть) пробрасываем,
     * чтобы планировщик повторил весь пост и не потерял обложку.
     *
     * @return байты объекта; {@code null}, если картинки нет.
     * @throws RuntimeException при временном сбое чтения из S3.
     */
    public byte[] bytes(String previewImageUrl) {
        String key = key(previewImageUrl);
        if (key == null) {
            return null;
        }
        try {
            return s3Client.getObjectAsBytes(request -> request.bucket(bucket).key(key)).asByteArray();
        } catch (NoSuchKeyException ex) {
            log.warn("Обложка {} не найдена в S3 — пост уйдёт текстом", key);
            return null;
        }
    }

    /** Имя файла для Telegram (по расширению определяет тип). */
    public String filename(String previewImageUrl) {
        String key = key(previewImageUrl);
        if (key == null) {
            return "cover";
        }
        int slash = key.lastIndexOf('/');
        return slash >= 0 ? key.substring(slash + 1) : key;
    }

    /** {@code /s3/articles/x.webp} → {@code articles/x.webp}; для абсолютных/внешних URL — {@code null}. */
    private static String key(String previewImageUrl) {
        if (!StringUtils.hasText(previewImageUrl)) {
            return null;
        }
        int marker = previewImageUrl.indexOf("/s3/");
        return marker >= 0 ? previewImageUrl.substring(marker + 4) : null;
    }
}
