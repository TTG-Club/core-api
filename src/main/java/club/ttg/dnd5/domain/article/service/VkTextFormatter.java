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
 * Приводит разметку статьи/новости к плоскому тексту для стены ВКонтакте.
 * <p>
 * Пост на стене VK — это обычный текст без форматирования: жирный/курсив/подчёркивание VK в постах не
 * рендерит, поэтому все markdown-маркеры и теги {@code {@..}} снимаются, оставляя только содержимое.
 * Контент хранится массивом блоков (строки-абзацы и узлы-объекты: цитата/заголовок/разделитель);
 * {@link VttgMarkupConverter#toText} разворачивает блок в промежуточный markdown-подобный текст, из которого
 * мы убираем {@code **}/{@code *} и раскрываем ссылки.
 * <p>
 * Ссылки: внешние URL VK не умеет «маскировать» (подмена текста ссылкой работает только для внутренних
 * vk.com-ссылок), зато любой сырой URL в тексте VK делает кликабельным автоматически. Поэтому ссылку
 * рендерим как {@code метка (url)} (или просто {@code url}, если метки нет). Внутренние ссылки на разделы
 * сайта достраиваются до абсолютного URL.
 */
@RequiredArgsConstructor
@Component
public class VkTextFormatter {

    /** Вид блока контента. */
    private enum Kind { NORMAL, QUOTE, HEADING, SEPARATOR }

    /** Типы блочных узлов (алиасы фронтового диалекта). */
    private static final Set<String> QUOTE_TYPES = Set.of("quote", "blockquote", "q");
    private static final Set<String> HEADING_TYPES = Set.of("heading", "h");
    private static final Set<String> SEPARATOR_TYPES = Set.of("separator", "hr");
    /** Разделитель (в VK нет горизонтальной черты). */
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

    /**
     * Разметка → плоский текст для стены VK: блоки (абзац / цитата / заголовок / разделитель) склеены
     * пустой строкой. Пустой результат ({@code ""}) означает, что постить нечего.
     */
    public String toText(String markup) {
        StringBuilder sb = new StringBuilder();
        for (String block : render(markup)) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(block);
        }
        return sb.toString();
    }

    private List<String> render(String markup) {
        List<String> out = new ArrayList<>();
        for (Block block : blocks(markup)) {
            if (block.kind() == Kind.SEPARATOR) {
                out.add(DIVIDER);
                continue;
            }
            String text = clean(markupConverter.toText(preprocessLinks(block.markup())));
            if (text.isEmpty()) {
                continue;
            }
            String plain = plainFrom(text);
            // Заголовок VK ничем не выделить (нет форматирования) — остаётся отдельным абзацем; цитата —
            // с префиксом «» » к каждой строке (единственный доступный в плоском тексте маркер цитаты).
            out.add(block.kind() == Kind.QUOTE ? quote(plain) : plain);
        }
        return out;
    }

    /** Оборачивает блок в цитату: префикс {@code » } к каждой строке (в т.ч. пустой). */
    private static String quote(String text) {
        String[] lines = text.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append("» ").append(lines[i]);
        }
        return sb.toString();
    }

    /**
     * markdown-подобный текст блока → плоский текст VK: раскрываем ссылки в {@code метка (url)}, снимаем
     * {@code **}/{@code *} и литеральные теги {@code {@..}} (оставляя содержимое).
     * <p>
     * Ссылки вырезаем в плейсхолдеры ДО снятия эмфазиса и возвращаем ПОСЛЕ: иначе {@code *} внутри URL
     * (напр. {@code .../a*b*c}) попал бы под {@link #MD_ITALIC} и адрес бы испортился. Из метки эмфазис
     * снимаем (VK его всё равно не рендерит), URL оставляем как есть.
     */
    private String plainFrom(String text) {
        List<String> links = new ArrayList<>();
        Matcher link = MD_LINK.matcher(text);
        StringBuilder masked = new StringBuilder();
        while (link.find()) {
            String label = stripEmphasis(link.group(1).trim());
            String url = link.group(2).trim();
            links.add((label.isEmpty() || label.equals(url)) ? url : label + " (" + url + ")");
            // Плейсхолдер без markdown-метасимволов — переживёт снятие эмфазиса нетронутым (\0 в тексте не встречается).
            link.appendReplacement(masked, Matcher.quoteReplacement("\u0000" + (links.size() - 1) + "\u0000"));
        }
        link.appendTail(masked);

        String result = stripTags(stripEmphasis(masked.toString()));
        for (int i = 0; i < links.size(); i++) {
            result = result.replace("\u0000" + i + "\u0000", links.get(i));
        }
        return result;
    }

    /** Снимает markdown-эмфазис {@code **жирный**}/{@code *курсив*} (VK его не рендерит), оставляя содержимое. */
    private static String stripEmphasis(String text) {
        text = MD_BOLD.matcher(text).replaceAll("$1");
        text = MD_ITALIC.matcher(text).replaceAll("$1");
        return text;
    }

    /** Снимает литеральные теги {@code {@name ...}}, оставляя их содержимое; раскрывает вложенность изнутри наружу. */
    private String stripTags(String text) {
        for (int pass = 0; pass < MAX_NESTING && text.indexOf("{@") >= 0; pass++) {
            Matcher matcher = TAG.matcher(text);
            if (!matcher.find()) {
                break;
            }
            matcher.reset();
            StringBuilder result = new StringBuilder();
            while (matcher.find()) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(2)));
            }
            matcher.appendTail(result);
            text = result.toString();
        }
        return text;
    }

    /**
     * Пред-обработка ДО {@link VttgMarkupConverter#toText}: маркеры-ссылки {@code {@type label | url:...}}
     * → markdown {@code [label](абсолютный url)}. Нужна потому, что toText теряет ссылки: у обычного
     * {@code {@link}} выкидывает url, а секции со спейсовым {@code | url:} не распознаёт. Остальные маркеры
     * ({@code {@b}}/{@code {@u}}/…) не трогаем — их снимет {@link #stripTags}.
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
     * База для абсолютных ссылок на разделы сайта. Ссылки идут в публичное сообщество, поэтому нужен
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

    private static boolean looksLikeJson(String s) {
        return (s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]"));
    }
}
