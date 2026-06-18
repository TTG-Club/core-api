package club.ttg.dnd5.domain.vttg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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
            "doc", "blockquote", "bulletList", "orderedList", "listItem"
    );
    private static final Pattern SITE_LINK = Pattern.compile(
            "\\{@(glossary|spell)\\s+([^|}]+)\\|url:([^}]+)}"
    );
    private static final Map<String, String> SITE_LINK_SECTIONS = siteLinkSections();
    private static final Pattern ITALIC = Pattern.compile("\\{@i\\s+([^}]+)}");
    private static final Pattern BOLD = Pattern.compile("\\{@b\\s+([^}]+)}");
    private static final Pattern ROLL = Pattern.compile("\\{@roll\\s+([^|}]+)(?:\\|[^}]*)?}");
    /** Универсальная ссылка {@code {@link термин|url:...}} (или без url) — в VTTG раскрывается в обычный текст термина. */
    private static final Pattern LINK = Pattern.compile("\\{@link\\s+([^|}]+)(?:\\|[^}]*)?}");

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
            String text = node.hasNonNull("text") ? node.get("text").asText() : "";
            String content = node.has("content")
                    ? extractChildren(node.get("content"), hasBlockContent(type))
                    : "";

            if ("hardBreak".equals(type)) {
                return "\n";
            }
            if ("listItem".equals(type) && StringUtils.hasText(content)) {
                return "- " + content.replace("\n\n", "\n  ");
            }
            return text + content;
        }
        return "";
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
        String formatted = replaceInline(text, ITALIC, "*$1*");
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
