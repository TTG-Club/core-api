package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.domain.vttg.service.VttgMarkupConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Приводит разметку статьи/новости к тексту для Discord (обычное сообщение, не embed).
 * <p>
 * Контент хранится массивом блоков: строки-абзацы и узлы-объекты (цитата/список/таблица/заголовок).
 * {@link VttgMarkupConverter#toText} разворачивает блок в промежуточный markdown-подобный текст
 * ({@code **жирный**}, {@code *курсив*}, {@code [метка](url)}). Discord-разметка почти совпадает с ним,
 * поэтому {@code **}/{@code *} оставляем как есть; переводим лишь литеральные маркеры {@code {@u}}/
 * {@code {@s}}/{@code {@spoiler}}/{@code {@code}} в Discord-markdown и цитату — в префикс {@code > }.
 * <p>
 * Ссылки: постим вебхуком, а у вебхуков/ботов Discord рендерит «маскированные» ссылки {@code [метка](url)}
 * в обычном сообщении — поэтому кликабельной делаем саму метку-слово. URL оборачиваем в {@code <>}
 * ({@code [метка](<url>)}): гасит громоздкую карточку-превью и не даёт скобкам в URL ломать разметку.
 * Внутренние ссылки на разделы сайта достраиваются до абсолютного URL.
 * <p>
 * Лимит Discord считается по «сырой» длине строки (символы {@code **}/{@code <>} тоже учитываются),
 * поэтому режем по этой длине, а не по видимой — в отличие от Telegram-HTML.
 */
@RequiredArgsConstructor
@Component
public class DiscordMarkdownFormatter {

    /** Вид блока контента. */
    private enum Kind { NORMAL, QUOTE, HEADING, SEPARATOR }

    /** Типы блочных узлов (алиасы фронтового диалекта). */
    private static final Set<String> QUOTE_TYPES = Set.of("quote", "blockquote", "q");
    private static final Set<String> HEADING_TYPES = Set.of("heading", "h");
    private static final Set<String> SEPARATOR_TYPES = Set.of("separator", "hr");
    /** Разделитель (в Discord нет горизонтальной черты). */
    private static final String DIVIDER = "———";

    private static final Pattern MD_BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*", Pattern.DOTALL);
    private static final Pattern MD_ITALIC = Pattern.compile("\\*(.+?)\\*", Pattern.DOTALL);
    private static final Pattern MD_LINK = Pattern.compile("\\[([^\\]]+)]\\(([^)]+)\\)");
    /** Самый внутренний тег {@code {@name content}} (content без вложенных фигурных скобок; может быть пустым). */
    private static final Pattern TAG = Pattern.compile("\\{@(\\w+)\\s*([^{}]*)}");
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

    private final VttgMarkupConverter markupConverter;
    private final ObjectMapper objectMapper;

    @Value("${app.url:https://ttg.club}")
    private String appUrl;

    /** Разметка → обычный текст без форматирования (для оценки видимой длины и проверки на пустоту). */
    public String toPlain(String markup) {
        StringBuilder sb = new StringBuilder();
        for (Rendered block : render(markup)) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(block.plain());
        }
        return sb.toString();
    }

    /**
     * Разметка → список сообщений Discord-markdown: первое ≤ {@code firstLimit} символов (в нём публикатор
     * добавит заголовок), последующие ≤ {@code restLimit}. Режем по границам блоков (абзацев); блок крупнее
     * {@code restLimit} дробим по словам плоским текстом. Так длинная новость не обрезается, а досылается
     * частями. Первый кусок может быть пустым — тогда первое сообщение = только заголовок.
     */
    public List<String> toMarkdownChunks(String markup, int firstLimit, int restLimit) {
        List<String> chunks = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int curLen = 0;
        for (Rendered block : render(markup)) {
            List<Rendered> pieces = block.md().length() <= restLimit
                    ? List.of(block)
                    : splitPlain(block.plain(), restLimit);
            for (Rendered piece : pieces) {
                int limit = chunks.isEmpty() ? firstLimit : restLimit;
                int pieceLen = piece.md().length();
                if (curLen == 0) {
                    if (pieceLen > limit) {
                        // Не влезает в первый кусок — оставляем его пустым, кладём со следующего.
                        chunks.add("");
                    }
                    cur.append(piece.md());
                    curLen = pieceLen;
                } else if (curLen + 2 + pieceLen <= limit) {
                    cur.append("\n\n").append(piece.md());
                    curLen += 2 + pieceLen;
                } else {
                    chunks.add(cur.toString());
                    cur.setLength(0);
                    cur.append(piece.md());
                    curLen = pieceLen;
                }
            }
        }
        if (cur.length() > 0 || chunks.isEmpty()) {
            chunks.add(cur.toString());
        }
        return chunks;
    }

    /** Один отрендеренный блок: Discord-markdown, его плоский текст и длина (по сырому markdown). */
    private record Rendered(String md, String plain) {
    }

    private List<Rendered> render(String markup) {
        List<Rendered> out = new ArrayList<>();
        for (Block block : blocks(markup)) {
            if (block.kind() == Kind.SEPARATOR) {
                out.add(new Rendered(DIVIDER, DIVIDER));
                continue;
            }
            String text = clean(markupConverter.toText(preprocessLinks(block.markup())));
            if (text.isEmpty()) {
                continue;
            }
            String plain = plainFrom(text);
            String tagized = tagize(text);
            String md = switch (block.kind()) {
                case QUOTE -> quote(tagized);
                // Заголовок → жирная строка; внутренние ** убираем, чтобы не ломать вложенным жирным.
                case HEADING -> "**" + tagized.replace("**", "") + "**";
                default -> tagized;
            };
            out.add(new Rendered(md, plain));
        }
        return out;
    }

    /** Дробит плоский текст на куски ≤ limit по границам слов (формат теряется — только для огромных блоков). */
    private static List<Rendered> splitPlain(String plain, int limit) {
        List<Rendered> pieces = new ArrayList<>();
        String rest = plain.strip();
        while (!rest.isEmpty()) {
            if (rest.length() <= limit) {
                pieces.add(new Rendered(rest, rest));
                break;
            }
            int cut = rest.lastIndexOf(' ', limit);
            if (cut <= 0) {
                cut = limit;
            }
            if (Character.isHighSurrogate(rest.charAt(cut - 1))) {
                cut--;
            }
            String piece = rest.substring(0, cut).strip();
            pieces.add(new Rendered(piece, piece));
            rest = rest.substring(cut).strip();
        }
        return pieces;
    }

    /** Оборачивает блок в цитату Discord: префикс {@code > } к каждой строке (в т.ч. пустой). */
    private static String quote(String md) {
        String[] lines = md.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append("> ").append(lines[i]);
        }
        return sb.toString();
    }

    /**
     * Пред-обработка ДО {@link VttgMarkupConverter#toText}: маркеры-ссылки {@code {@type label | url:...}}
     * → markdown {@code [label](абсолютный url)}. Нужна потому, что toText теряет ссылки: у обычного
     * {@code {@link}} выкидывает url, а секции со спейсовым {@code | url:} не распознаёт. Остальные маркеры
     * ({@code {@b}}/{@code {@u}}/…) не трогаем — их разберут toText и {@link #applyTags}.
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
     * База для абсолютных ссылок на разделы сайта. Ссылки идут в публичный канал, поэтому нужен
     * полноценный публичный URL: если {@code app.url} без схемы или localhost (локальная разработка) —
     * ведём на боевой сайт (иначе относительный href будет неполноценным).
     */
    private String site() {
        String url = appUrl == null ? "" : appUrl.trim();
        boolean usable = (url.startsWith("http://") || url.startsWith("https://")) && !url.contains("localhost");
        if (!usable) {
            url = "https://ttg.club";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /** Блок контента: исходная разметка блока и его вид. */
    private record Block(String markup, Kind kind) {
    }

    /**
     * Разбивает контент на блоки. Хранимая форма — JSON-массив: строки-абзацы и узлы-объекты
     * (цитата/заголовок/разделитель — по {@code type}). Не-массив (обычная строка) — один блок.
     */
    private List<Block> blocks(String markup) {
        if (!StringUtils.hasText(markup)) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(markup);
            if (root.isArray()) {
                List<Block> list = new ArrayList<>();
                for (JsonNode element : root) {
                    list.add(new Block(element.toString(), kindOf(element)));
                }
                return list;
            }
        } catch (Exception ignored) {
            // Не JSON — обычная строка-разметка, обрабатываем одним блоком.
        }
        return List.of(new Block(markup, Kind.NORMAL));
    }

    private static Kind kindOf(JsonNode element) {
        if (!element.isObject()) {
            return Kind.NORMAL;
        }
        String type = element.path("type").asText();
        if (QUOTE_TYPES.contains(type)) {
            return Kind.QUOTE;
        }
        if (HEADING_TYPES.contains(type)) {
            return Kind.HEADING;
        }
        if (SEPARATOR_TYPES.contains(type)) {
            return Kind.SEPARATOR;
        }
        return Kind.NORMAL;
    }

    /** Нормализует результат toText: {@code <br>} → перевод строки; сырую JSON-разметку считаем пустой. */
    private String clean(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        text = text.replace("<br>", "\n").strip();
        return looksLikeJson(text) ? "" : text;
    }

    /** markdown-подобный текст блока → Discord-markdown: раскрываем ссылки и литеральные теги {@code {@..}}. */
    private String tagize(String text) {
        return applyTags(convertLinks(text), true);
    }

    /** Плоский текст блока: снимаем {@code **}/{@code *}, ссылки сводим к метке, теги {@code {@..}} — к содержимому. */
    private String plainFrom(String text) {
        text = MD_BOLD.matcher(text).replaceAll("$1");
        text = MD_ITALIC.matcher(text).replaceAll("$1");
        text = MD_LINK.matcher(text).replaceAll("$1");
        return applyTags(text, false);
    }

    /** {@code [метка](url)} → маскированная ссылка {@code [метка](<url>)} (или {@code <url>}, если метки нет). */
    private String convertLinks(String text) {
        Matcher link = MD_LINK.matcher(text);
        StringBuilder result = new StringBuilder();
        while (link.find()) {
            link.appendReplacement(result,
                    Matcher.quoteReplacement(linkText(link.group(1).trim(), link.group(2).trim())));
        }
        link.appendTail(result);
        return result.toString();
    }

    /**
     * {@code [метка](url)} → маскированная ссылка Discord, где кликабельна сама метка-слово (у вебхуков/ботов
     * это работает и в обычном сообщении). http(s)-URL оборачиваем в {@code <>}: гасит громоздкую карточку-превью
     * и не даёт скобкам/спецсимволам в URL ломать разметку. Если метки нет (пустая/совпадает с url) — показываем
     * сам URL (кликабелен, без превью).
     */
    private static String linkText(String label, String url) {
        boolean http = url.startsWith("http://") || url.startsWith("https://");
        String target = http ? "<" + url + ">" : url;
        if (label.isEmpty() || label.equals(url)) {
            return target;
        }
        return "[" + label + "](" + target + ")";
    }

    /**
     * Переводит литеральные теги {@code {@name ...}} в Discord-markdown (или снимает их), раскрывая
     * вложенность изнутри наружу. {@code md=false} — просто снимает теги, оставляя содержимое.
     */
    private String applyTags(String text, boolean md) {
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
                matcher.appendReplacement(result, Matcher.quoteReplacement(wrap(tag, content, md)));
            }
            matcher.appendTail(result);
            text = result.toString();
        }
        return text;
    }

    private String wrap(String tag, String content, boolean md) {
        if (!md || !StringUtils.hasText(content)) {
            return content;
        }
        return switch (tag) {
            case "b", "bold" -> "**" + content + "**";
            case "i", "italic" -> "*" + content + "*";
            case "u", "underline" -> "__" + content + "__";
            case "s", "strike", "strikethrough" -> "~~" + content + "~~";
            case "kbd", "code" -> "`" + content + "`";
            case "spoiler" -> "||" + content + "||";
            case "h", "heading" -> "**" + content + "**";
            // sup/sub/highlight/mark/badge/roll/dice/separator и прочее не выражаем в Discord-markdown —
            // оставляем только содержимое (без тега), чтобы ничего не текло сырым.
            default -> content;
        };
    }

    private static boolean looksLikeJson(String s) {
        return (s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]"));
    }
}
