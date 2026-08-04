package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.domain.vttg.service.VttgMarkupConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверяет сборку страницы Instant View: строки промежуточного текста должны собираться обратно в
 * блочный HTML (абзацы, списки, таблицы), по которому пишется шаблон на instantview.telegram.org.
 * {@code app.url} в тестах не задан — конвертер и форматтер берут боевой сайт по умолчанию.
 */
class InstantViewHtmlFormatterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final InstantViewHtmlFormatter formatter =
            new InstantViewHtmlFormatter(new VttgMarkupConverter(objectMapper), objectMapper);

    @Test
    void paragraphsAndInlineFormattingBecomeHtml() {
        String html = formatter.toHtml("""
                ["Обычный абзац.", "Абзац с {@b жирным} и {@i курсивом}."]
                """);

        assertEquals("<p>Обычный абзац.</p><p>Абзац с <b>жирным</b> и <i>курсивом</i>.</p>", html);
    }

    @Test
    void headingKeepsLevelBelowTitle() {
        String html = formatter.toHtml("""
                [{"type": "heading", "attrs": {"level": 3}, "content": [{"type": "text", "text": "Компендиум"}]}]
                """);

        // <h1> занят названием записи, поэтому уровни сдвинуты на единицу.
        assertEquals("<h4>Компендиум</h4>", html);
    }

    @Test
    void listNodeBecomesUnorderedList() {
        String html = formatter.toHtml("""
                [{"type": "list", "content": [
                    {"type": "li", "content": [{"type": "text", "text": "Первый пункт"}]},
                    {"type": "li", "content": [{"type": "text", "text": "Второй пункт"}]}
                ]}]
                """);

        assertEquals("<ul><li>Первый пункт</li><li>Второй пункт</li></ul>", html);
    }

    @Test
    void orderedListKeepsItsKind() {
        String html = formatter.toHtml("""
                [{"type": "list", "attrs": {"type": "ordered"}, "content": [
                    {"type": "li", "content": [{"type": "text", "text": "Раз"}]},
                    {"type": "li", "content": [{"type": "text", "text": "Два"}]}
                ]}]
                """);

        assertEquals("<ol><li>Раз</li><li>Два</li></ol>", html);
    }

    @Test
    void quoteAndSeparatorBecomeBlocks() {
        String html = formatter.toHtml("""
                [{"type": "quote", "content": [{"type": "text", "text": "Цитата"}]}, {"type": "separator"}]
                """);

        assertEquals("<blockquote><p>Цитата</p></blockquote><hr>", html);
    }

    @Test
    void tableBecomesHtmlTableWithHeader() {
        String html = formatter.toHtml("""
                [{"type": "table", "colLabels": ["Кость", "Урон"], "rows": [["к6", "3"], ["к8", "4"]]}]
                """);

        assertEquals("<table><thead><tr><th>Кость</th><th>Урон</th></tr></thead>"
                + "<tbody><tr><td>к6</td><td>3</td></tr><tr><td>к8</td><td>4</td></tr></tbody></table>", html);
    }

    @Test
    void sectionMarkerBecomesAbsoluteLink() {
        String html = formatter.toHtml("""
                ["Смотри {@spell огненный шар|url:fire-ball}."]
                """);

        assertEquals("<p>Смотри <a href=\"https://ttg.club/spells/fire-ball\">огненный шар</a>.</p>", html);
    }

    @Test
    void unknownMarkerCollapsesToText() {
        String html = formatter.toHtml("""
                ["Бросок {@dice 2к6} урона."]
                """);

        assertFalse(html.contains("{@"), "сырые маркеры не должны попадать на страницу: " + html);
        assertEquals("<p>Бросок 2к6 урона.</p>", html);
    }

    @Test
    void markerAttributesDoNotLeakIntoText() {
        // Тело такого маркера — «метка|атрибут:значение»; на странице должна остаться только метка.
        String html = formatter.toHtml("""
                ["Диаметр {@dice 1к6 × 10|notation:1к6*10} футов."]
                """);

        assertEquals("<p>Диаметр 1к6 × 10 футов.</p>", html);
    }

    @Test
    void htmlSpecialCharactersAreEscaped() {
        String html = formatter.toHtml("""
                ["Тег <script> и амперсанд & в тексте"]
                """);

        assertEquals("<p>Тег &lt;script&gt; и амперсанд &amp; в тексте</p>", html);
    }

    @Test
    void plainTextDropsTagsAndEntities() {
        String plain = formatter.toPlain("""
                ["Абзац с {@b жирным} и знаком &.", {"type": "list", "content": [
                    {"type": "li", "content": [{"type": "text", "text": "Пункт"}]}
                ]}]
                """);

        assertEquals("Абзац с жирным и знаком &. Пункт", plain);
    }

    @Test
    void emptyMarkupGivesEmptyHtml() {
        assertTrue(formatter.toHtml(null).isEmpty());
        assertTrue(formatter.toHtml("").isEmpty());
        assertTrue(formatter.toHtml("[]").isEmpty());
    }
}
