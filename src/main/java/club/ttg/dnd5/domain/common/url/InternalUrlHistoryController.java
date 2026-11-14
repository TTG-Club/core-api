package club.ttg.dnd5.domain.common.url;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * Прежние адреса записей каталога для других сервисов (комментарии, баг-репорты),
 * которые хранят адрес страницы у себя и после смены адреса должны перенести свои
 * данные на новый.
 *
 * <p>Защита — общий секрет {@code X-Service-Token} ({@code InternalServiceTokenFilter}).</p>
 */
@Tag(name = "Internal: Прежние адреса записей")
@RestController
@RequestMapping("/api/internal/url-history")
public class InternalUrlHistoryController {
    private final JdbcTemplate jdbc;

    public InternalUrlHistoryController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Смена адреса записи. Цепочки схлопнуты: {@code newUrl} — всегда текущий адрес.
     *
     * @param section   раздел сайта ({@code spells}, {@code bestiary}, …) — первый сегмент пути страницы
     * @param oldUrl    прежний адрес
     * @param newUrl    текущий адрес
     * @param changedAt когда адрес сменился (для строки, обновлённой при повторной смене, — время последней)
     */
    public record UrlHistoryEntry(String section, String oldUrl, String newUrl, Instant changedAt) {
    }

    @Operation(summary = "Прежние адреса записей (межсервисный вызов)",
            description = "Все пары «прежний адрес → текущий». С since — только сменённые после этого момента.")
    @GetMapping
    public List<UrlHistoryEntry> history(
            @Parameter(description = "Вернуть только смены после этого момента (ISO-8601)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
        String sql = "select section, old_url, new_url, changed_at from entity_url_history"
                + (since == null ? "" : " where changed_at > ?")
                + " order by changed_at, section, old_url";
        Object[] args = since == null ? new Object[0] : new Object[]{Timestamp.from(since)};
        return jdbc.query(sql, (row, index) -> new UrlHistoryEntry(
                row.getString(1), row.getString(2), row.getString(3), row.getTimestamp(4).toInstant()), args);
    }
}
