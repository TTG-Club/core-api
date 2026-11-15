package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.domain.vttg.service.VttgMarkupConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Проверяет, что картинка в тексте (блочный узел {@code image}, алиас {@code img}) в пост ВКонтакте не
 * попадает: картинок внутри текста там нет, поэтому узел пропускается целиком — без адреса, подписи
 * и пустого абзаца.
 */
class VkTextFormatterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VkTextFormatter formatter =
            new VkTextFormatter(new VttgMarkupConverter(objectMapper), objectMapper);

    @Test
    void imageNodeIsSkippedWithoutTraceAndEmptyBlock() {
        String text = formatter.toText("""
                ["Первый абзац.",
                 {"type": "image", "attrs": {"url": "/s3/articles/user/map.webp", "float": "left", "width": 40,
                    "alt": "Карта долины", "caption": "Долина Ледяного Ветра"}},
                 "Второй абзац."]
                """);

        assertEquals("Первый абзац.\n\nВторой абзац.", text);
    }

    @Test
    void imageAliasAndImageOnlyContentGiveNoText() {
        // Пустой результат означает «постить нечего» — одна картинка текста не даёт.
        String text = formatter.toText("""
                [{"type": "img", "attrs": {"url": "https://example.com/a.png", "caption": "Подпись"}}]
                """);

        assertEquals("", text);
    }
}
