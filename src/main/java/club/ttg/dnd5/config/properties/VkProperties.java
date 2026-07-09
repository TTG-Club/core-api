package club.ttg.dnd5.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Настройки автопубликации статей / новостей на стену сообщества ВКонтакте.
 * <p>
 * Секреты ({@code accessToken}, {@code groupId}) приходят из окружения: в Dokploy — через переменные
 * окружения сервиса, локально — через {@code local.env} (в .gitignore). Интеграция включена ровно тогда,
 * когда заданы {@code accessToken} и {@code groupId}; если их нет (например, локально) — планировщик просто
 * ничего не отправляет.
 * <p>
 * {@code accessToken} — это ключ доступа сообщества («API-ключ группы»): бот не нужен, пост уходит от имени
 * сообщества ({@code wall.post} с {@code from_group=1}). Токен должен иметь право «Стена» (для постинга) и
 * «Фотографии» (для обложки). Загрузка обложки на стену через ключ сообщества доступна не всегда (VK может
 * ответить ошибкой 27) — тогда пост уходит текстом; для гарантированной обложки можно подставить пользовательский
 * токен администратора группы с правами {@code wall,photos,groups}.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "vk")
public class VkProperties {
    /**
     * Ключ доступа сообщества («API-ключ группы») или пользовательский токен админа группы.
     * Пусто — интеграция выключена. Секрет.
     */
    private String accessToken;

    /**
     * Числовой id сообщества (без знака). Нужен для {@code owner_id=-<groupId>} в постах и как {@code group_id}
     * при загрузке обложки. Пусто — интеграция выключена.
     */
    private String groupId;

    /** База VK API. Меняется только для тестов/прокси. */
    private String apiUrl = "https://api.vk.com/method";

    /** Версия VK API (обязательный параметр {@code v} каждого вызова). */
    private String apiVersion = "5.199";

    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(10);

    /** Сколько записей отправлять за один тик планировщика (страховка от заливки стены пачкой). */
    private int batchSize = 10;
}
