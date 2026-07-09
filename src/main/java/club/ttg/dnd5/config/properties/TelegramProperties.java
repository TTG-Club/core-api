package club.ttg.dnd5.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Настройки автопубликации статей / новостей в Telegram-канал.
 * <p>
 * Секреты ({@code botToken}, {@code chatId}) приходят из окружения: в Dokploy — через
 * переменные окружения сервиса, локально — через {@code local.env} (в .gitignore).
 * Интеграция включена ровно тогда, когда заданы {@code botToken} и {@code chatId}; если их нет
 * (например, локально) — планировщик просто ничего не отправляет.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "telegram")
public class TelegramProperties {
    /** Токен бота (формат {@code 123456:AA...}). Пусто — интеграция выключена. */
    private String botToken;

    /** Канал: {@code @username} или числовой id вида {@code -100...}. */
    private String chatId;

    /** База Bot API. Меняется только для тестов/прокси. */
    private String apiUrl = "https://api.telegram.org";

    /** Режим разметки текста поста. HTML проще экранировать, чем MarkdownV2. */
    private String parseMode = "HTML";

    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(10);

    /** Сколько записей отправлять за один тик планировщика (страховка от заливки канала пачкой). */
    private int batchSize = 10;
}
