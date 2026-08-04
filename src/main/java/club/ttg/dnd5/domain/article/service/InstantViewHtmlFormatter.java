package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.domain.vttg.service.VttgMarkupConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Приводит разметку статьи / новости к HTML страницы Instant View (см. {@link ArticleInstantViewService}).
 * <p>
 * Разбор тот же, что у {@link TelegramHtmlFormatter}: контент — массив блоков (строки-абзацы и узлы
 * цитаты/заголовка/списка/таблицы), каждый блок разворачивается
 * {@link VttgMarkupConverter#toTextKeepingMarkers} в промежуточный markdown-подобный текст
 * ({@code **жирный**}, {@code *курсив*}, {@code [метка](url)}, списки {@code - }/{@code N. },
 * таблицы {@code | ячейка |}). Разница в цели: пост в канале — это плоский текст, а страница IV
 * должна быть СТРУКТУРНОЙ, поэтому строки собираются обратно в блочный HTML
 * ({@code <p>}, {@code <ul>}/{@code <ol>}, {@code <table>}, {@code <blockquote>}, {@code <h2>}) —
 * именно по нему шаблон Instant View разбирает статью.
 * <p>
 * Набор тегов ограничен теми, что понимает формат Instant View; всё остальное схлопывается в текст,
 * чтобы сырые маркеры не попали на страницу.
 */
@RequiredArgsConstructor
@Component
public class InstantViewHtmlFormatter {

    /** Типы блочных узлов (алиасы фронтового диалекта, см. MARKER_CONFIGS на фронте). */
    private static final Set<String> QUOTE_TYPES = Set.of("quote", "blockquote", "q");
    private static final Set<String> HEADING_TYPES = Set.of("heading", "h");
    private static final Set<String> SEPARATOR_TYPES = Set.of("separator", "hr");

    private static final Pattern MD_BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*", Pattern.DOTALL);
    private static final Pattern MD_ITALIC = Pattern.compile("\\*(.+?)\\*", Pattern.DOTALL);
    private static final Pattern MD_LINK = Pattern.compile("\\[([^\\]]+)]\\(([^)]+)\\)");
    /** Самый внутренний тег {@code {@name content}} (content без вложенных фигурных скобок). */
    private static final Pattern TAG = Pattern.compile("\\{@(\\w+)\\s*([^{}]*)}");
    /** Открывающий/закрывающий инлайн-тег (только те, что вставляем сами) — для проверки вложенности. */
    private static final Pattern HTML_TAG =
            Pattern.compile("</?(b|i|u|s|a|code|mark|sup|sub)\\b[^>]*>");
    private static final int MAX_NESTING = 8;

    /** Инлайн-маркер {@code {@type тело}} (тело без вложенных фигурных скобок). */
    private static final Pattern MARKER = Pattern.compile("\\{@([\\w-]+)\\s+([^{}]*)}");
    /** Тип ссылки-раздела → путь раздела на сайте (по фронтовой MARKER_URL_MAP). */
    private static final Map<String, String> SECTION_PATHS = Map.ofEntries(
            Map.entry("class", "classes"), Map.entry("spell", "spells"), Map.entry("feat", "feats"),
            Map.entry("background", "backgrounds"), Map.entry("magicItem", "magic-items"),
            Map.entry("magic-item", "magic-items"), Map.entry("item", "items"),
            Map.entry("creature", "bestiary"), Map.entry("bestiary", "bestiary"),
            Map.entry("glossary", "glossary"));
    /** Обычная (внешняя/произвольная) ссылка. */
    private static final Set<String> LINK_TYPES = Set.of("link", "a");

    /** Пункт списка: {@code - текст} либо {@code 12. текст} (так их отдаёт конвертер). */
    private static final Pattern LIST_ITEM = Pattern.compile("^\\s{0,3}(-|\\d+\\.)\\s+");
    /** Строка-разделитель markdown-таблицы: {@code | --- | :--: |}. */
    private static final Pattern TABLE_SEPARATOR = Pattern.compile("^\\|[\\s:|-]+\\|$");
    /** Сущности, которые вставляет {@link #escape} — для обратного разбора в {@link #toPlain}. */
    private static final Pattern ENTITY = Pattern.compile("&(amp|lt|gt|quot);");

    private final VttgMarkupConverter markupConverter;
    private final ObjectMapper objectMapper;

    @Value("${app.url:https://ttg.club}")
    private String appUrl;

    /** Разметка → блочный HTML тела статьи (пустая строка, если текста нет). */
    public String toHtml(String markup) {
        StringBuilder html = new StringBuilder();
        for (JsonNode block : blocks(markup)) {
            html.append(renderBlock(block));
        }
        return html.toString();
    }

    /** Разметка → обычный текст без тегов (для {@code og:description} и подобного). */
    public String toPlain(String markup) {
        String text = toHtml(markup)
                .replaceAll("</(p|h[1-6]|li|blockquote|tr)>", " ")
                .replaceAll("<[^>]+>", "");
        text = ENTITY.matcher(text).replaceAll(match -> switch (match.group(1)) {
            case "amp" -> "&";
            case "lt" -> "<";
            case "gt" -> ">";
            default -> "\"";
        });
        return text.replaceAll("\\s+", " ").strip();
    }

    /**
     * Разбивает контент на блоки. Хранимая форма — JSON-массив: строки-абзацы и узлы-объекты
     * (цитата/заголовок/разделитель/список/таблица — по {@code type}). Не-массив (обычная строка) —
     * один блок.
     */
    private List<JsonNode> blocks(String markup) {
        if (!StringUtils.hasText(markup)) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(markup);
            if (root.isArray()) {
                List<JsonNode> list = new ArrayList<>();
                root.forEach(list::add);
                return list;
            }
        } catch (Exception ignored) {
            // Не JSON — обычная строка-разметка, обрабатываем одним блоком.
        }
        return List.of(TextNode.valueOf(markup));
    }

    private String renderBlock(JsonNode block) {
        String type = block.isObject() ? block.path("type").asText("") : "";
        if (SEPARATOR_TYPES.contains(type)) {
            return "<hr>";
        }

        String text = clean(markupConverter.toTextKeepingMarkers(preprocessLinks(block.toString())));
        if (text.isEmpty()) {
            return "";
        }
        if (QUOTE_TYPES.contains(type)) {
            return "<blockquote>" + body(text) + "</blockquote>";
        }
        if (HEADING_TYPES.contains(type)) {
            // Уровень заголовка статьи (1..4) со сдвигом: <h1> занят названием записи.
            int level = Math.min(4, Math.max(1, block.path("attrs").path("level").asInt(1)) + 1);
            // Внутренний <b> убираем: заголовок и так выделен.
            String inline = inline(text.replace("\n", " ")).replace("<b>", "").replace("</b>", "");
            return "<h" + level + ">" + inline + "</h" + level + ">";
        }
        return body(text);
    }

    /**
     * Строки промежуточного текста → блочный HTML: подряд идущие пункты собираются в
     * {@code <ul>}/{@code <ol>}, строки markdown-таблицы — в {@code <table>}, остальное — в {@code <p>}.
     */
    private String body(String text) {
        List<String> lines = List.of(text.split("\n"));
        StringBuilder out = new StringBuilder();
        int index = 0;
        while (index < lines.size()) {
            String line = lines.get(index);
            if (!StringUtils.hasText(line)) {
                index++;
            } else if (isTableRow(line)) {
                index = appendTable(out, lines, index);
            } else if (LIST_ITEM.matcher(line).find()) {
                index = appendList(out, lines, index);
            } else {
                out.append("<p>").append(inline(line.strip())).append("</p>");
                index++;
            }
        }
        return out.toString();
    }

    /**
     * Собирает подряд идущие пункты одного вида в список. Продолжение многострочного пункта
     * конвертер отбивает двумя пробелами ({@code indentContinuation}) — такие строки приклеиваем
     * к текущему пункту через {@code <br>}. Возвращает индекс первой строки ЗА списком.
     */
    private int appendList(StringBuilder out, List<String> lines, int start) {
        boolean ordered = ordered(lines.get(start));
        out.append(ordered ? "<ol>" : "<ul>");

        StringBuilder item = null;
        int index = start;
        while (index < lines.size()) {
            String line = lines.get(index);
            Matcher marker = LIST_ITEM.matcher(line);
            if (marker.find()) {
                if (ordered(line) != ordered) {
                    // Сменился вид списка — закрываем текущий, соседний соберёт следующий проход.
                    break;
                }
                if (item != null) {
                    out.append("<li>").append(item).append("</li>");
                }
                item = new StringBuilder(inline(line.substring(marker.end()).strip()));
            } else if (item != null && line.startsWith("  ") && StringUtils.hasText(line)) {
                item.append("<br>").append(inline(line.strip()));
            } else {
                break;
            }
            index++;
        }
        if (item != null) {
            out.append("<li>").append(item).append("</li>");
        }

        out.append(ordered ? "</ol>" : "</ul>");
        return index;
    }

    /**
     * Собирает подряд идущие строки markdown-таблицы в {@code <table>}: первая строка становится
     * шапкой, если под ней есть строка-разделитель (конвертер её всегда ставит). Возвращает индекс
     * первой строки ЗА таблицей.
     */
    private int appendTable(StringBuilder out, List<String> lines, int start) {
        List<List<String>> rows = new ArrayList<>();
        boolean header = false;
        int index = start;
        while (index < lines.size() && isTableRow(lines.get(index))) {
            String line = lines.get(index).strip();
            if (TABLE_SEPARATOR.matcher(line).matches()) {
                header = !rows.isEmpty();
            } else {
                rows.add(cells(line));
            }
            index++;
        }
        if (rows.isEmpty()) {
            return index;
        }

        out.append("<table>");
        int first = 0;
        if (header) {
            out.append("<thead><tr>");
            rows.getFirst().forEach(cell -> out.append("<th>").append(inline(cell)).append("</th>"));
            out.append("</tr></thead>");
            first = 1;
        }
        out.append("<tbody>");
        for (int row = first; row < rows.size(); row++) {
            out.append("<tr>");
            rows.get(row).forEach(cell -> out.append("<td>").append(inline(cell)).append("</td>"));
            out.append("</tr>");
        }
        out.append("</tbody></table>");
        return index;
    }

    private static boolean ordered(String line) {
        Matcher marker = LIST_ITEM.matcher(line);
        return marker.find() && marker.group(1).endsWith(".");
    }

    private static boolean isTableRow(String line) {
        String trimmed = line.strip();
        return trimmed.startsWith("|") && trimmed.endsWith("|") && trimmed.length() > 1;
    }

    /** {@code | a | b |} → ячейки без обрамляющих труб. */
    private static List<String> cells(String line) {
        String inner = line.substring(1, line.length() - 1);
        List<String> cells = new ArrayList<>();
        for (String cell : inner.split("\\|", -1)) {
            cells.add(cell.strip());
        }
        return cells;
    }

    /** Нормализует результат конвертера: {@code <br>} → перевод строки; сырую JSON-разметку считаем пустой. */
    private String clean(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        text = text.replace("<br>", "\n").strip();
        return looksLikeJson(text) ? "" : text;
    }

    /** markdown-подобная строка → инлайн-HTML; при кривой вложенности — экранированный плоский текст. */
    private String inline(String text) {
        String html = applyTags(applyMarkdown(escape(text)), true);
        // Перекрывающийся markdown («**жирный *курсив***») даёт невалидную вложенность —
        // жертвуем оформлением строки ради корректной страницы.
        return wellFormed(html) ? html : escape(applyTags(plainMarkdown(text), false));
    }

    private String applyMarkdown(String text) {
        text = MD_BOLD.matcher(text).replaceAll("<b>$1</b>");
        text = MD_ITALIC.matcher(text).replaceAll("<i>$1</i>");

        Matcher link = MD_LINK.matcher(text);
        StringBuilder result = new StringBuilder();
        while (link.find()) {
            String label = link.group(1);
            String url = link.group(2).replace("\"", "&quot;");
            link.appendReplacement(result, Matcher.quoteReplacement("<a href=\"" + url + "\">" + label + "</a>"));
        }
        link.appendTail(result);
        return result.toString();
    }

    private static String plainMarkdown(String text) {
        text = MD_BOLD.matcher(text).replaceAll("$1");
        text = MD_ITALIC.matcher(text).replaceAll("$1");
        return MD_LINK.matcher(text).replaceAll("$1");
    }

    /**
     * Переводит литеральные теги {@code {@name ...}} в HTML (или в текст), раскрывая вложенность
     * изнутри наружу. {@code html=false} — просто снимает теги, оставляя содержимое.
     */
    private String applyTags(String text, boolean html) {
        for (int pass = 0; pass < MAX_NESTING && text.indexOf("{@") >= 0; pass++) {
            Matcher matcher = TAG.matcher(text);
            if (!matcher.find()) {
                break;
            }
            matcher.reset();
            StringBuilder result = new StringBuilder();
            while (matcher.find()) {
                String tag = matcher.group(1).toLowerCase();
                String content = matcher.group(2);
                matcher.appendReplacement(result, Matcher.quoteReplacement(wrap(tag, content, html)));
            }
            matcher.appendTail(result);
            text = result.toString();
        }
        return text;
    }

    private String wrap(String tag, String content, boolean html) {
        if (!StringUtils.hasText(content)) {
            return content;
        }
        String htmlTag = switch (tag) {
            case "b", "bold" -> "b";
            case "i", "italic", "em" -> "i";
            case "u", "underline" -> "u";
            case "s", "strike", "strikethrough" -> "s";
            case "kbd", "code" -> "code";
            case "sup" -> "sup";
            case "sub" -> "sub";
            case "mark", "highlight" -> "mark";
            case "h", "heading" -> "b";
            default -> null;
        };
        if (htmlTag == null) {
            // spoiler/badge/dice и прочее в Instant View не выразить — остаётся видимая метка. Тело
            // у таких маркеров устроено как «метка|атрибут:значение» ({@dice 1к6|notation:1к6*10}),
            // поэтому берём часть до трубы: служебные атрибуты в текст статьи попадать не должны.
            return label(content);
        }
        // html=false — оформление просто снимаем, тело оформления остаётся целиком (в нём | обычный символ).
        return html ? tagged(htmlTag, content) : content;
    }

    private static String tagged(String tag, String content) {
        return "<" + tag + ">" + content + "</" + tag + ">";
    }

    /** Проверяет, что вставленные инлайн-теги правильно вложены и закрыты. */
    private static boolean wellFormed(String html) {
        Deque<String> stack = new ArrayDeque<>();
        Matcher matcher = HTML_TAG.matcher(html);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (matcher.group().startsWith("</")) {
                if (stack.isEmpty() || !stack.pop().equals(name)) {
                    return false;
                }
            } else {
                stack.push(name);
            }
        }
        return stack.isEmpty();
    }

    /**
     * Пред-обработка ДО {@link VttgMarkupConverter#toTextKeepingMarkers}: маркеры-ссылки
     * {@code {@type label | url:...}} → markdown {@code [label](абсолютный url)}. Нужна потому, что
     * toText теряет ссылки: у обычного {@code {@link}} выкидывает url, а секции со спейсовым
     * {@code | url:} не распознаёт (та же причина, что в {@link TelegramHtmlFormatter}).
     */
    private String preprocessLinks(String markup) {
        if (markup == null || markup.indexOf("{@") < 0) {
            return markup;
        }
        Matcher matcher = MARKER.matcher(markup);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String href = href(matcher.group(1), matcher.group(2));
            String replacement = href != null
                    ? "[" + label(matcher.group(2)) + "](" + href + ")"
                    : matcher.group();
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /** Абсолютный href для маркера-ссылки; {@code null}, если это не ссылка (нет url или не тот тип). */
    private String href(String type, String body) {
        int pipe = body.indexOf('|');
        if (pipe < 0) {
            return null;
        }
        String attrs = body.substring(pipe + 1);
        String url = attr(attrs, "url");
        if (url == null) {
            url = attr(attrs, "href");
        }
        if (url == null) {
            return null;
        }
        if (LINK_TYPES.contains(type)) {
            // Обычная ссылка: абсолютную оставляем, относительный роут сайта достраиваем.
            return url.startsWith("http://") || url.startsWith("https://")
                    ? url
                    : site() + (url.startsWith("/") ? url : "/" + url);
        }
        String section = SECTION_PATHS.get(type);
        return section != null ? site() + "/" + section + "/" + url : null;
    }

    private static String label(String body) {
        int pipe = body.indexOf('|');
        return (pipe < 0 ? body : body.substring(0, pipe)).trim();
    }

    private static String attr(String attrs, String key) {
        for (String part : attrs.split("\\|")) {
            String trimmed = part.trim();
            if (trimmed.startsWith(key + ":")) {
                return trimmed.substring(key.length() + 1).trim();
            }
        }
        return null;
    }

    /**
     * База для абсолютных ссылок на разделы сайта. Страницу читают в Telegram, поэтому нужен
     * полноценный публичный URL: если {@code app.url} без схемы или localhost (локальная
     * разработка) — ведём на боевой сайт.
     */
    private String site() {
        String url = appUrl == null ? "" : appUrl.trim();
        boolean usable = (url.startsWith("http://") || url.startsWith("https://")) && !url.contains("localhost");
        if (!usable) {
            url = "https://ttg.club";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static boolean looksLikeJson(String s) {
        return (s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]"));
    }
}
