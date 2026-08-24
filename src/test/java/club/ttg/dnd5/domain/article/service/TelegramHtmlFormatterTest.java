package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.domain.vttg.service.VttgMarkupConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Проверяет обычный (не редакторский) текст на входе форматтера — так приходит короткое описание
 * поста с Instant View: его вводят простым текстовым полем, а не разметкой. Оно должно доехать до
 * канала как есть, но безопасно: спецсимволы HTML экранированы, выделение и маркеры развёрнуты
 * в Telegram-теги. {@code app.url} в тестах не задан — форматтер берёт боевой сайт по умолчанию.
 */
class TelegramHtmlFormatterTest {

    private static final int LIMIT = 4096;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TelegramHtmlFormatter formatter =
            new TelegramHtmlFormatter(new VttgMarkupConverter(objectMapper), objectMapper);

    @Test
    void plainSummaryKeepsLineBreaksAndEscapesHtml() {
        List<String> chunks = formatter.toHtmlChunks("""
                Вышло обновление <бестиария>.
                Полсотни существ & новые фильтры.""", LIMIT, LIMIT);

        assertEquals(List.of("""
                Вышло обновление &lt;бестиария&gt;.
                Полсотни существ &amp; новые фильтры."""), chunks);
    }

    @Test
    void plainSummarySupportsMarkdownAndMarkers() {
        List<String> chunks = formatter.toHtmlChunks(
                "Главное: **новый раздел** и {@i немного} правок.", LIMIT, LIMIT);

        assertEquals(List.of("Главное: <b>новый раздел</b> и <i>немного</i> правок."), chunks);
    }

    @Test
    void plainSummaryTurnsSectionMarkerIntoSiteLink() {
        List<String> chunks = formatter.toHtmlChunks(
                "Добавили {@spell Огненный шар|url:fire-ball}.", LIMIT, LIMIT);

        assertEquals(
                List.of("Добавили <a href=\"https://ttg.club/spells/fire-ball\">Огненный шар</a>."),
                chunks);
    }

    @Test
    void emptySummaryGivesSingleEmptyChunk() {
        assertEquals(List.of(""), formatter.toHtmlChunks("   ", LIMIT, LIMIT));
    }
}
