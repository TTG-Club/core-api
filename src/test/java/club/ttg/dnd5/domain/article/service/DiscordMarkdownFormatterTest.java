package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.domain.vttg.service.VttgMarkupConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Проверяет, что картинка в тексте (блочный узел {@code image}, алиас {@code img}) в пост Discord не
 * попадает: картинок внутри текста там нет, поэтому узел пропускается целиком — без адреса, подписи
 * и пустого абзаца.
 */
class DiscordMarkdownFormatterTest {

    private static final int LIMIT = 2000;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DiscordMarkdownFormatter formatter =
            new DiscordMarkdownFormatter(new VttgMarkupConverter(objectMapper), objectMapper);

    @Test
    void imageNodeIsSkippedWithoutTraceAndEmptyBlock() {
        String markup = """
                ["Первый абзац.",
                 {"type": "image", "attrs": {"url": "/s3/articles/user/map.webp", "float": "left", "width": 40,
                    "alt": "Карта долины", "caption": "Долина Ледяного Ветра"}},
                 "Второй абзац."]
                """;

        assertEquals(List.of("Первый абзац.\n\nВторой абзац."), formatter.toMarkdownChunks(markup, LIMIT, LIMIT));
        // В длину сообщения картинка тоже не входит: плоский текст — только два абзаца.
        assertEquals("Первый абзац.\n\nВторой абзац.", formatter.toPlain(markup));
    }

    @Test
    void imageAliasAndImageOnlyContentGiveNoText() {
        String markup = """
                [{"type": "img", "attrs": {"url": "https://example.com/a.png", "caption": "Подпись"}}]
                """;

        assertEquals(List.of(""), formatter.toMarkdownChunks(markup, LIMIT, LIMIT));
        assertEquals("", formatter.toPlain(markup));
    }
}
