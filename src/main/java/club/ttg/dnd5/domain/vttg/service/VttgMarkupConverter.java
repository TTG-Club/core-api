package club.ttg.dnd5.domain.vttg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class VttgMarkupConverter {
    private static final Set<String> BLOCK_CONTENT_TYPES = Set.of(
            "doc", "blockquote", "bulletList", "orderedList", "listItem", "table", "tableRow"
    );
    private static final Map<String, String> SITE_LINK_SECTIONS = siteLinkSections();
    /**
     * Ссылка на раздел сайта: {@code {@item кинжал|url:dagger-phb}}. Типы — ключи
     * {@link #SITE_LINK_SECTIONS}: шаблон не должен хватать маркер другого рода с
     * похожим телом. Метка — без фигурных скобок, чтобы вложенный маркер достался
     * общему разбору, а не уехал внутрь текста ссылки. Пробелы вокруг {@code |} и
     * после {@code url:} допускаются — в контенте встречается и такое написание.
     */
    private static final Pattern SITE_LINK = Pattern.compile(
            "\\{@(" + String.join("|", SITE_LINK_SECTIONS.keySet()) + ")"
                    + "\\s+([^|{}]+)\\|\\s*url:\\s*([^}|]+)}"
    );
    /** Перенос строки {@code {@br}} — в VTTG раскрывается в обычный перевод строки. */
    private static final Pattern BR = Pattern.compile("\\{@br}");
    /**
     * Маркер {@code {@тип тело}}; тело — без вложенных скобок, поэтому совпадает самый
     * внутренний. Разбор идёт повторными проходами изнутри наружу, так что вложенное
     * оформление ({@code {@b важно: {@u прочти}}}) раскрывается целиком — регэксп с
     * {@code [^}]} на такой вложенности рвал текст по первой закрывающей скобке.
     */
    private static final Pattern MARKER = Pattern.compile("\\{@([\\w-]+)(?:\\s+([^{}]*))?}");
    /** Предел проходов раскрытия вложенных маркеров (как в форматтерах статей). */
    private static final int MAX_MARKER_NESTING = 8;
    /**
     * Маркеры, у которых нет своего вида ни в одной целевой разметке — остаётся только
     * метка. Броски: интерактивными их оставляет лишь {@link #toTextKeepingRolls}.
     * {@code {@link}}: ведёт на произвольный роут сайта, а не на карточку сущности.
     */
    private static final Set<String> LABEL_ONLY_MARKERS = Set.of("roll", "link");

    private final ObjectMapper objectMapper;
    @Value("${app.url:https://ttg.club}")
    private String appUrl;

    /**
     * Куда поедет результат — от этого зависит, насколько разворачивать оформление.
     *
     * <p>{@code VTTG} — прямо в компендиум. Оформление раскрывается в разметку, которую
     * понимает рендерер VTTG (markdown с GFM плюс инлайновый HTML для того, чего в
     * markdown нет), а маркер, которому вида не нашлось, сворачивается в свою метку:
     * служебные фигурные скобки в тексте карточки не нужны.</p>
     *
     * <p>{@code INTERMEDIATE} — вызывающему, который разбирает оформление сам под свою
     * целевую разметку (форматтеры статей для Discord/Telegram/VK). Раскрываются только
     * жирный и курсив — на них эти форматтеры и рассчитывают; остальные маркеры доезжают
     * целыми, иначе {@code {@u ...}} или {@code {@spoiler ...}} потеряли бы вид.</p>
     */
    private enum Target { VTTG, INTERMEDIATE }

    public String toText(String markup) {
        return convert(markup, false, Target.VTTG);
    }

    /**
     * Как {@link #toText(String)}, но сохраняет inline-теги бросков {@code {@roll ...}}.
     * Нужно для форматов VTTG, где клиент сам отрисовывает интерактивные броски в описании
     * (например магические предметы — см. wands.json).
     */
    public String toTextKeepingRolls(String markup) {
        return convert(markup, true, Target.VTTG);
    }

    /**
     * Как {@link #toText(String)}, но отдаёт ПРОМЕЖУТОЧНЫЙ текст: маркеры оформления,
     * кроме жирного и курсива, остаются нетронутыми — вызывающий разворачивает их сам
     * под свою целевую разметку (форматтеры статей для Discord/Telegram/VK). Для VTTG
     * такой режим не подходит: там нераскрытый маркер уедет в компендиум как есть.
     */
    public String toTextKeepingMarkers(String markup) {
        return convert(markup, false, Target.INTERMEDIATE);
    }

    private String convert(String markup, boolean keepRolls, Target target) {
        if (!StringUtils.hasText(markup)) {
            return "";
        }

        try {
            String extracted = extract(objectMapper.readTree(markup), target).trim();
            String source = StringUtils.hasText(extracted) ? extracted : markup;
            return replaceMarkup(source, keepRolls, target);
        } catch (Exception ignored) {
            return replaceMarkup(markup, keepRolls, target);
        }
    }

    private String extract(JsonNode node, Target target) {
        if (node == null || node.isNull()) {
            return "";
        }
        if (node.isTextual()) {
            String value = node.textValue();
            if (looksLikeJson(value)) {
                try {
                    return extract(objectMapper.readTree(value), target);
                } catch (Exception ignored) {
                    return value;
                }
            }
            return value;
        }
        if (node.isArray()) {
            return extractChildren(node, true, target);
        }
        if (node.isObject()) {
            String type = node.path("type").asText();

            // Перенос строки — и ProseMirror (hardBreak), и фронтовый узел (break).
            if ("hardBreak".equals(type) || "break".equals(type)) {
                return "\n";
            }
            // Списки: ProseMirror (bulletList/orderedList) и фронтовый {type:list}.
            if ("bulletList".equals(type)) {
                return extractList(node, false, target);
            }
            if ("orderedList".equals(type)) {
                return extractList(node, true, target);
            }
            if ("list".equals(type)) {
                return extractFrontendList(node, target);
            }
            // Таблица: фронтовая форма (colLabels/rows) либо ProseMirror (content).
            if ("table".equals(type)) {
                return node.has("colLabels") || node.has("rows")
                        ? extractFrontendTable(node, target)
                        : extractTable(node, target);
            }
            // Инлайн-узлы фронтового диалекта (формат/ссылки) — узловая форма тех
            // же тегов, что в строках-абзацах идут литералами и разворачиваются в
            // replaceMarkup. null — не инлайн-узел, идёт обобщённо (текст+контент).
            String inline = extractInlineNode(node, type, target);
            if (inline != null) {
                return inline;
            }

            String text = node.hasNonNull("text") ? node.get("text").asText() : "";
            String content = node.has("content")
                    ? extractChildren(node.get("content"), hasBlockContent(type), target)
                    : "";
            return text + content;
        }
        return "";
    }

    private String extractList(JsonNode node, boolean ordered, Target target) {
        JsonNode items = node.get("content");
        if (items == null || !items.isArray()) {
            return "";
        }

        int start = node.path("attrs").path("start").asInt(1);
        List<String> lines = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            String content = extractListItem(items.get(index), target);
            if (StringUtils.hasText(content)) {
                String marker = ordered ? (start + index) + ". " : "- ";
                lines.add(marker + indentContinuation(content));
            }
        }
        return String.join("\n", lines);
    }

    private String extractListItem(JsonNode item, Target target) {
        if (item == null || !item.isObject()) {
            return extract(item, target).trim();
        }

        JsonNode content = item.get("content");
        if (content == null) {
            return "";
        }
        return extractChildren(content, true, target).trim();
    }

    private String indentContinuation(String content) {
        return content.trim().replace("\n", "\n  ");
    }

    /**
     * Список фронтового диалекта {@code {type:list, attrs:{type}, content:[...]}}:
     * пункт — это МАССИВ-батч инлайна либо узел {@code {type:li}}. В markdown —
     * маркеры {@code - } / {@code N. } (как {@link #extractList}).
     */
    private String extractFrontendList(JsonNode node, Target target) {
        JsonNode items = node.get("content");
        if (items == null || !items.isArray()) {
            return "";
        }

        boolean ordered = "ordered".equals(node.path("attrs").path("type").asText());
        List<String> lines = new ArrayList<>();
        int number = 1;
        for (JsonNode item : items) {
            String content = extractFrontendListItem(item, target).trim();
            if (StringUtils.hasText(content)) {
                String marker = ordered ? (number++) + ". " : "- ";
                lines.add(marker + indentContinuation(content));
            }
        }
        return String.join("\n", lines);
    }

    /** Содержимое пункта фронтового списка: батч-массив, узел {@code {type:li}} или иной узел. */
    private String extractFrontendListItem(JsonNode item, Target target) {
        if (item.isArray()) {
            return extractChildren(item, false, target);
        }
        if ("li".equals(item.path("type").asText())) {
            return extractContent(item, target);
        }
        return extract(item, target);
    }

    /**
     * Таблица фронтового диалекта {@code {type:table, colLabels[], colStyles[],
     * rows[][]}} → markdown-таблица. Ячейка — строка либо {@code {content, align}}.
     * Стили колонок и подпись в VTTG опускаются (как и в {@link #extractTable}).
     */
    private String extractFrontendTable(JsonNode node, Target target) {
        List<List<String>> table = new ArrayList<>();

        JsonNode colLabels = node.get("colLabels");
        if (colLabels != null && colLabels.isArray()) {
            List<String> header = new ArrayList<>();
            colLabels.forEach(label -> header.add(formatTableCell(extractInlineCell(label, target))));
            if (!header.isEmpty()) {
                table.add(header);
            }
        }

        JsonNode rows = node.get("rows");
        if (rows != null && rows.isArray()) {
            for (JsonNode row : rows) {
                if (!row.isArray()) {
                    continue;
                }
                List<String> cells = new ArrayList<>();
                row.forEach(cell -> cells.add(formatTableCell(extractInlineCell(cell, target))));
                if (!cells.isEmpty()) {
                    table.add(cells);
                }
            }
        }

        if (table.isEmpty()) {
            return "";
        }

        int width = table.stream().mapToInt(List::size).max().orElse(0);
        List<String> markdown = new ArrayList<>();
        markdown.add(formatTableRow(table.getFirst(), width));
        markdown.add(formatTableSeparator(width));
        for (int index = 1; index < table.size(); index++) {
            markdown.add(formatTableRow(table.get(index), width));
        }
        return String.join("\n", markdown);
    }

    /**
     * Инлайн-текст ячейки/заголовка фронтовой таблицы: строка, массив инлайн-узлов
     * (так фронт сериализует {@code colLabels[i]}/ячейку) либо {@code {content, align}}.
     * Всегда инлайн-склейка (без блочного {@code \n\n}), иначе в ячейке из нескольких
     * фрагментов (например {@code {@th Урон ({@dice к6})}}) появился бы ложный перенос.
     */
    private String extractInlineCell(JsonNode cell, Target target) {
        if (cell.isArray()) {
            return extractChildren(cell, false, target);
        }
        if (cell.isObject() && cell.has("content") && !cell.has("type")) {
            return extractChildren(cell.get("content"), false, target);
        }
        return extract(cell, target);
    }

    /**
     * Разворачивает ИНЛАЙН-узел фронтового диалекта в текст/markdown. Возвращает
     * null, если это не инлайн-узел (обрабатывается обобщённо). Ссылки и оформление
     * берутся из тех же таблиц, что и литеральные {@code {@...}} в строках
     * ({@link #SITE_LINK_SECTIONS}, {@link #formatting}): один и тот же текст обязан
     * выглядеть одинаково, в каком бы из двух диалектов его ни сохранили. Здесь разбор
     * идёт рекурсией по вложенным узлам — без риска регэкспа на «}».
     */
    private String extractInlineNode(JsonNode node, String type, Target target) {
        // Ссылки на разделы сайта ({type:item|spell|glossary|…}, attrs.url) — как в
        // replaceSiteLinks; прочие типы разделов идут обобщённо (текст).
        String section = SITE_LINK_SECTIONS.get(type);
        if (section != null) {
            String label = extractContent(node, target).trim();
            String url = node.path("attrs").path("url").asText("");
            return url.isEmpty()
                    ? label
                    : "[" + label + "](" + siteUrl() + "/" + section + "/" + url + ")";
        }
        if ("link".equals(type)) {
            return extractContent(node, target);
        }
        Wrap wrap = formatting(type, target);
        return wrap == null ? null : wrap.around(extractContent(node, target));
    }

    /** Инлайн-содержимое узла (content) без блочных переносов; "" если пусто. */
    private String extractContent(JsonNode node, Target target) {
        JsonNode content = node.get("content");
        return content == null ? "" : extractChildren(content, false, target);
    }

    private String extractTable(JsonNode node, Target target) {
        JsonNode rows = node.get("content");
        if (rows == null || !rows.isArray()) {
            return "";
        }

        List<List<String>> table = new ArrayList<>();
        for (JsonNode row : rows) {
            List<String> cells = extractTableRow(row, target);
            if (!cells.isEmpty()) {
                table.add(cells);
            }
        }
        if (table.isEmpty()) {
            return "";
        }

        int width = table.stream().mapToInt(List::size).max().orElse(0);
        List<String> markdown = new ArrayList<>();
        markdown.add(formatTableRow(table.getFirst(), width));
        markdown.add(formatTableSeparator(width));
        for (int index = 1; index < table.size(); index++) {
            markdown.add(formatTableRow(table.get(index), width));
        }
        return String.join("\n", markdown);
    }

    private List<String> extractTableRow(JsonNode row, Target target) {
        JsonNode cells = row.get("content");
        if (cells == null || !cells.isArray()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        cells.forEach(cell -> result.add(formatTableCell(extract(cell, target))));
        return result;
    }

    private String formatTableCell(String cell) {
        return cell.trim()
                .replace("|", "\\|")
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace("\n\n", "<br>")
                .replace("\n", "<br>");
    }

    private String formatTableRow(List<String> row, int width) {
        List<String> cells = new ArrayList<>(row);
        while (cells.size() < width) {
            cells.add("");
        }
        return "| " + String.join(" | ", cells) + " |";
    }

    private String formatTableSeparator(int width) {
        return "| " + String.join(" | ", Collections.nCopies(width, "---")) + " |";
    }

    private String extractChildren(JsonNode children, boolean blockContent, Target target) {
        if (!children.isArray()) {
            return extract(children, target);
        }

        List<String> parts = new ArrayList<>();
        children.forEach(child -> {
            String value = extract(child, target);
            if (!value.isEmpty()) {
                parts.add(blockContent ? value.trim() : value);
            }
        });
        return String.join(blockContent ? "\n\n" : "", parts);
    }

    private boolean hasBlockContent(String type) {
        return BLOCK_CONTENT_TYPES.contains(type);
    }

    private boolean looksLikeJson(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String trimmed = value.trim();
        return (trimmed.startsWith("[") && trimmed.endsWith("]"))
                || (trimmed.startsWith("{") && trimmed.endsWith("}"));
    }

    private String replaceMarkup(String text, boolean keepRolls, Target target) {
        String formatted = replaceInline(text, BR, "\n");
        // Ссылки — до общего разбора: иначе от {@item ...|url:...} осталась бы метка.
        formatted = replaceSiteLinks(formatted);

        return expandMarkers(formatted, keepRolls, target);
    }

    /**
     * Разворачивает маркеры {@code {@тип тело}} повторными проходами изнутри наружу —
     * так вложенное оформление раскрывается целиком, а не рвётся по первой закрывающей
     * скобке. Проходы прекращаются, как только очередной ничего не заменил.
     *
     * @param text      текст с уже разобранными ссылками на разделы сайта
     * @param keepRolls сохранять ли inline-теги {@code {@roll ...}}
     * @param target    куда поедет результат (см. {@link Target})
     */
    private String expandMarkers(String text, boolean keepRolls, Target target) {
        String current = text;
        for (int pass = 0; pass < MAX_MARKER_NESTING && current.contains("{@"); pass++) {
            Matcher matcher = MARKER.matcher(current);
            StringBuilder result = new StringBuilder();
            boolean replaced = false;
            while (matcher.find()) {
                String tag = matcher.group(1).toLowerCase(Locale.ROOT);
                String expanded = expandMarker(tag, matcher.group(2), keepRolls, target);
                if (expanded == null) {
                    continue;
                }
                matcher.appendReplacement(result, Matcher.quoteReplacement(expanded));
                replaced = true;
            }
            if (!replaced) {
                return current;
            }
            matcher.appendTail(result);
            current = result.toString();
        }
        return current;
    }

    /**
     * Раскрытие одного маркера; {@code null} — оставить маркер как есть.
     *
     * <p>У тега оформления тело берётся целиком: это проза, в которой {@code |} — обычный
     * символ. У остальных тело устроено как {@code метка|атрибут:значение}, поэтому
     * остаётся только метка.</p>
     *
     * @param tag       тип маркера в нижнем регистре
     * @param body      тело маркера ({@code null}, если его нет)
     * @param keepRolls сохранять ли inline-теги {@code {@roll ...}}
     * @param target    куда поедет результат (см. {@link Target})
     */
    private static String expandMarker(String tag, String body, boolean keepRolls, Target target) {
        if (keepRolls && "roll".equals(tag)) {
            return null;
        }
        Wrap wrap = formatting(tag, target);
        if (wrap != null) {
            return wrap.around(body == null ? "" : body.trim());
        }
        // Незнакомый маркер сворачивается в метку только для VTTG: промежуточному
        // потребителю он нужен целым, чтобы развернуть под свою разметку самому.
        String label = markerLabel(body);
        return target == Target.VTTG || LABEL_ONLY_MARKERS.contains(tag) ? label : null;
    }

    /** Обёртка оформления в целевой разметке. */
    private record Wrap(String open, String close) {
        /** Оборачивает содержимое; пустое оставляет пустым, чтобы не родить голые «****». */
        private String around(String content) {
            return content.isEmpty() ? content : open + content + close;
        }
    }

    /**
     * Обёртка для тега оформления либо {@code null}, если тег не про оформление.
     *
     * <p>Жирный и курсив раскрываются всегда: на них рассчитывают форматтеры статей,
     * которые читают из промежуточного текста только {@code **}, {@code *} и
     * {@code [](…)}. Остальное — только для VTTG: там рендерер разбирает markdown с
     * GFM и вставляет результат как HTML, поэтому зачёркнутый и код выражаются
     * markdown'ом, а подчёркнутый, индексы и выделение — инлайновым HTML, которого в
     * markdown нет. Промежуточному потребителю они уходят маркерами: {@code ~~} и
     * {@code <u>} он не понимает и показал бы их сырыми.</p>
     *
     * @param tag    тип маркера в нижнем регистре либо тип инлайн-узла
     * @param target куда поедет результат (см. {@link Target})
     */
    private static Wrap formatting(String tag, Target target) {
        Wrap core = switch (tag) {
            case "b", "bold" -> new Wrap("**", "**");
            case "i", "italic" -> new Wrap("*", "*");
            default -> null;
        };
        if (core != null || target != Target.VTTG) {
            return core;
        }
        return switch (tag) {
            case "em" -> new Wrap("*", "*");
            // Заголовок внутри абзаца отдельным уровнем не выразить — остаётся жирным.
            case "h", "heading" -> new Wrap("**", "**");
            case "s", "strike", "strikethrough" -> new Wrap("~~", "~~");
            case "code", "kbd" -> new Wrap("`", "`");
            case "u", "underline" -> new Wrap("<u>", "</u>");
            case "sup" -> new Wrap("<sup>", "</sup>");
            case "sub" -> new Wrap("<sub>", "</sub>");
            case "mark", "highlight" -> new Wrap("<mark>", "</mark>");
            default -> null;
        };
    }

    /** Видимая метка маркера: тело до первого {@code |}; пустая, если тела нет. */
    private static String markerLabel(String body) {
        if (body == null) {
            return "";
        }
        int pipe = body.indexOf('|');
        return (pipe < 0 ? body : body.substring(0, pipe)).trim();
    }

    private String replaceSiteLinks(String text) {
        Matcher matcher = SITE_LINK.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String type = matcher.group(1).trim();
            String label = matcher.group(2).trim();
            String url = matcher.group(3).trim();
            String section = SITE_LINK_SECTIONS.get(type);
            // Шаблон построен по ключам карты, поэтому раздел здесь всегда известен;
            // ветка с меткой — страховка на случай расхождения.
            String replacement = section == null
                    ? label
                    : "[" + label + "](" + siteUrl() + "/" + section + "/" + url + ")";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String replaceInline(String text, Pattern pattern, String replacement) {
        return pattern.matcher(text).replaceAll(replacement);
    }

    private String siteUrl() {
        String value = StringUtils.hasText(appUrl) ? appUrl.trim() : "https://ttg.club";
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    /**
     * Тип маркера-ссылки → путь раздела на сайте. Повторяет фронтовую
     * {@code MARKER_URL_MAP} (тот же список, что в форматтерах статей): в контенте
     * встречаются ссылки на любой раздел, а не только на глоссарий и заклинания —
     * например стартовое снаряжение предыстории состоит из {@code {@item ...}}.
     */
    private static Map<String, String> siteLinkSections() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("class", "classes");
        result.put("spell", "spells");
        result.put("feat", "feats");
        result.put("background", "backgrounds");
        result.put("magicItem", "magic-items");
        result.put("magic-item", "magic-items");
        result.put("item", "items");
        result.put("creature", "bestiary");
        result.put("bestiary", "bestiary");
        result.put("glossary", "glossary");
        return result;
    }
}
