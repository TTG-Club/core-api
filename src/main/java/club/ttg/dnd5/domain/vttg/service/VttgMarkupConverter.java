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
    private static final Pattern SITE_LINK = Pattern.compile(
            "\\{@(glossary|spell)\\s+([^|}]+)\\|url:([^}]+)}"
    );
    private static final Map<String, String> SITE_LINK_SECTIONS = siteLinkSections();
    private static final Pattern ITALIC = Pattern.compile("\\{@i\\s+([^}]+)}");
    private static final Pattern BOLD = Pattern.compile("\\{@b\\s+([^}]+)}");
    private static final Pattern ROLL = Pattern.compile("\\{@roll\\s+([^|}]+)(?:\\|[^}]*)?}");
    /** Generic <code>{&#64;link term|url:...}</code> link; VTTG receives only the visible term text. */
    private static final Pattern LINK = Pattern.compile("\\{@link\\s+([^|}]+)(?:\\|[^}]*)?}");
    /** Перенос строки {@code {@br}} — в VTTG раскрывается в обычный перевод строки. */
    private static final Pattern BR = Pattern.compile("\\{@br}");

    private final ObjectMapper objectMapper;
    @Value("${app.url:https://ttg.club}")
    private String appUrl;

    public String toText(String markup) {
        return convert(markup, false);
    }

    /**
     * Как {@link #toText(String)}, но сохраняет inline-теги бросков {@code {@roll ...}}.
     * Нужно для форматов VTTG, где клиент сам отрисовывает интерактивные броски в описании
     * (например магические предметы — см. wands.json).
     */
    public String toTextKeepingRolls(String markup) {
        return convert(markup, true);
    }

    private String convert(String markup, boolean keepRolls) {
        if (!StringUtils.hasText(markup)) {
            return "";
        }

        try {
            String extracted = extract(objectMapper.readTree(markup)).trim();
            return replaceMarkup(StringUtils.hasText(extracted) ? extracted : markup, keepRolls);
        } catch (Exception ignored) {
            return replaceMarkup(markup, keepRolls);
        }
    }

    private String extract(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }
        if (node.isTextual()) {
            String value = node.textValue();
            if (looksLikeJson(value)) {
                try {
                    return extract(objectMapper.readTree(value));
                } catch (Exception ignored) {
                    return value;
                }
            }
            return value;
        }
        if (node.isArray()) {
            return extractChildren(node, true);
        }
        if (node.isObject()) {
            String type = node.path("type").asText();

            // Перенос строки — и ProseMirror (hardBreak), и фронтовый узел (break).
            if ("hardBreak".equals(type) || "break".equals(type)) {
                return "\n";
            }
            // Списки: ProseMirror (bulletList/orderedList) и фронтовый {type:list}.
            if ("bulletList".equals(type)) {
                return extractList(node, false);
            }
            if ("orderedList".equals(type)) {
                return extractList(node, true);
            }
            if ("list".equals(type)) {
                return extractFrontendList(node);
            }
            // Таблица: фронтовая форма (colLabels/rows) либо ProseMirror (content).
            if ("table".equals(type)) {
                return node.has("colLabels") || node.has("rows")
                        ? extractFrontendTable(node)
                        : extractTable(node);
            }
            // Инлайн-узлы фронтового диалекта (формат/ссылки) — узловая форма тех
            // же тегов, что в строках-абзацах идут литералами и разворачиваются в
            // replaceMarkup. null — не инлайн-узел, идёт обобщённо (текст+контент).
            String inline = extractInlineNode(node, type);
            if (inline != null) {
                return inline;
            }

            String text = node.hasNonNull("text") ? node.get("text").asText() : "";
            String content = node.has("content")
                    ? extractChildren(node.get("content"), hasBlockContent(type))
                    : "";
            return text + content;
        }
        return "";
    }

    private String extractList(JsonNode node, boolean ordered) {
        JsonNode items = node.get("content");
        if (items == null || !items.isArray()) {
            return "";
        }

        int start = node.path("attrs").path("start").asInt(1);
        List<String> lines = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            String content = extractListItem(items.get(index));
            if (StringUtils.hasText(content)) {
                String marker = ordered ? (start + index) + ". " : "- ";
                lines.add(marker + indentContinuation(content));
            }
        }
        return String.join("\n", lines);
    }

    private String extractListItem(JsonNode item) {
        if (item == null || !item.isObject()) {
            return extract(item).trim();
        }

        JsonNode content = item.get("content");
        if (content == null) {
            return "";
        }
        return extractChildren(content, true).trim();
    }

    private String indentContinuation(String content) {
        return content.trim().replace("\n", "\n  ");
    }

    /**
     * Список фронтового диалекта {@code {type:list, attrs:{type}, content:[...]}}:
     * пункт — это МАССИВ-батч инлайна либо узел {@code {type:li}}. В markdown —
     * маркеры {@code - } / {@code N. } (как {@link #extractList}).
     */
    private String extractFrontendList(JsonNode node) {
        JsonNode items = node.get("content");
        if (items == null || !items.isArray()) {
            return "";
        }

        boolean ordered = "ordered".equals(node.path("attrs").path("type").asText());
        List<String> lines = new ArrayList<>();
        int number = 1;
        for (JsonNode item : items) {
            String content = extractFrontendListItem(item).trim();
            if (StringUtils.hasText(content)) {
                String marker = ordered ? (number++) + ". " : "- ";
                lines.add(marker + indentContinuation(content));
            }
        }
        return String.join("\n", lines);
    }

    /** Содержимое пункта фронтового списка: батч-массив, узел {@code {type:li}} или иной узел. */
    private String extractFrontendListItem(JsonNode item) {
        if (item.isArray()) {
            return extractChildren(item, false);
        }
        if ("li".equals(item.path("type").asText())) {
            return extractContent(item);
        }
        return extract(item);
    }

    /**
     * Таблица фронтового диалекта {@code {type:table, colLabels[], colStyles[],
     * rows[][]}} → markdown-таблица. Ячейка — строка либо {@code {content, align}}.
     * Стили колонок и подпись в VTTG опускаются (как и в {@link #extractTable}).
     */
    private String extractFrontendTable(JsonNode node) {
        List<List<String>> table = new ArrayList<>();

        JsonNode colLabels = node.get("colLabels");
        if (colLabels != null && colLabels.isArray()) {
            List<String> header = new ArrayList<>();
            colLabels.forEach(label -> header.add(formatTableCell(extractInlineCell(label))));
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
                row.forEach(cell -> cells.add(formatTableCell(extractInlineCell(cell))));
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
    private String extractInlineCell(JsonNode cell) {
        if (cell.isArray()) {
            return extractChildren(cell, false);
        }
        if (cell.isObject() && cell.has("content") && !cell.has("type")) {
            return extractChildren(cell.get("content"), false);
        }
        return extract(cell);
    }

    /**
     * Разворачивает ИНЛАЙН-узел фронтового диалекта в текст/markdown. Возвращает
     * null, если это не инлайн-узел (обрабатывается обобщённо). Форматирование
     * повторяет {@link #replaceMarkup} (там — литеральные {@code {@...}} из строк),
     * но здесь рекурсирует по вложенным узлам без риска регэкспа на «}».
     */
    private String extractInlineNode(JsonNode node, String type) {
        if ("bold".equals(type)) {
            return "**" + extractContent(node) + "**";
        }
        if ("italic".equals(type)) {
            return "*" + extractContent(node) + "*";
        }
        // Ссылки на разделы сайта ({type:glossary|spell}, attrs.url) — как в
        // replaceSiteLinks; прочие типы разделов/форматов идут обобщённо (текст).
        String section = SITE_LINK_SECTIONS.get(type);
        if (section != null) {
            String label = extractContent(node).trim();
            String url = node.path("attrs").path("url").asText("");
            return url.isEmpty()
                    ? label
                    : "[" + label + "](" + siteUrl() + "/" + section + "/" + url + ")";
        }
        if ("link".equals(type)) {
            return extractContent(node);
        }
        return null;
    }

    /** Инлайн-содержимое узла (content) без блочных переносов; "" если пусто. */
    private String extractContent(JsonNode node) {
        JsonNode content = node.get("content");
        return content == null ? "" : extractChildren(content, false);
    }

    private String extractTable(JsonNode node) {
        JsonNode rows = node.get("content");
        if (rows == null || !rows.isArray()) {
            return "";
        }

        List<List<String>> table = new ArrayList<>();
        for (JsonNode row : rows) {
            List<String> cells = extractTableRow(row);
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

    private List<String> extractTableRow(JsonNode row) {
        JsonNode cells = row.get("content");
        if (cells == null || !cells.isArray()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        cells.forEach(cell -> result.add(formatTableCell(extract(cell))));
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

    private String extractChildren(JsonNode children, boolean blockContent) {
        if (!children.isArray()) {
            return extract(children);
        }

        List<String> parts = new ArrayList<>();
        children.forEach(child -> {
            String value = extract(child);
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

    private String replaceMarkup(String text, boolean keepRolls) {
        String formatted = replaceInline(text, BR, "\n");
        formatted = replaceInline(formatted, ITALIC, "*$1*");
        formatted = replaceInline(formatted, BOLD, "**$1**");
        if (!keepRolls) {
            formatted = unwrapLabel(formatted, ROLL);
        }
        formatted = unwrapLabel(formatted, LINK);

        return replaceSiteLinks(formatted);
    }

    /** Заменяет каждое совпадение на его первую группу (метку), без обрамляющих тегов. */
    private String unwrapLabel(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(1).trim()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String replaceSiteLinks(String text) {
        Matcher matcher = SITE_LINK.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String type = matcher.group(1).trim();
            String label = matcher.group(2).trim();
            String url = matcher.group(3).trim();
            String section = SITE_LINK_SECTIONS.get(type);
            String replacement = section == null
                    ? matcher.group()
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

    private static Map<String, String> siteLinkSections() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("glossary", "glossary");
        result.put("spell", "spells");
        return result;
    }
}
