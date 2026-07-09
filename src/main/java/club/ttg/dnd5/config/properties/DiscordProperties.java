package club.ttg.dnd5.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Настройки автопубликации статей / новостей в Discord-канал через входящий вебхук.
 * <p>
 * Секрет ({@code webhookUrl}) приходит из окружения: в Dokploy — через переменную окружения сервиса,
 * локально — через {@code local.env} (в .gitignore). Интеграция включена ровно тогда, когда задан
 * {@code webhookUrl}; если его нет (например, локально) — планировщик просто ничего не отправляет.
 * <p>
 * В отличие от Telegram (bot-token + chat-id) вебхук самодостаточен: весь адрес канала зашит в URL,
 * поэтому отдельного идентификатора канала не нужно — «просто подключить вебхук к каналу».
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "discord")
public class DiscordProperties {
    /**
     * Полный URL входящего вебхука Discord ({@code https://discord.com/api/webhooks/<id>/<token>}).
     * Пусто — интеграция выключена. Секрет: содержит токен вебхука.
     */
    private String webhookUrl;

    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(10);

    /** Сколько записей отправлять за один тик планировщика (страховка от заливки канала пачкой). */
    private int batchSize = 10;
}
