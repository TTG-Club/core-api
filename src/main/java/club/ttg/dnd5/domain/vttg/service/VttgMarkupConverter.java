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
            if ("bulletList".equals(type)) {
                return extractList(node, false);
            }
            if ("orderedList".equals(type)) {
                return extractList(node, true);
            }
            if ("table".equals(type)) {
                return extractTable(node);
            }
            String text = node.hasNonNull("text") ? node.get("text").asText() : "";
            String content = node.has("content")
                    ? extractChildren(node.get("content"), hasBlockContent(type))
                    : "";

            if ("hardBreak".equals(type)) {
                return "\n";
            }
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
