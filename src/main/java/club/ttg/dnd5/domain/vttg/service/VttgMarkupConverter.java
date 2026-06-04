package club.ttg.dnd5.domain.vttg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class VttgMarkupConverter {
    private static final Set<String> BLOCK_CONTENT_TYPES = Set.of(
            "doc", "blockquote", "bulletList", "orderedList", "listItem"
    );
    private static final Pattern GLOSSARY_LINK = Pattern.compile(
            "\\{@glossary\\s+([^|}]+)\\|url:([^}]+)}"
    );
    private static final Pattern ITALIC = Pattern.compile("\\{@i\\s+([^}]+)}");
    private static final Pattern BOLD = Pattern.compile("\\{@b\\s+([^}]+)}");

    private final ObjectMapper objectMapper;
    @Value("${app.url:https://ttg.club}")
    private String appUrl;

    public String toText(String markup) {
        if (!StringUtils.hasText(markup)) {
            return "";
        }

        try {
            String extracted = extract(objectMapper.readTree(markup)).trim();
            return replaceMarkup(StringUtils.hasText(extracted) ? extracted : markup);
        } catch (Exception ignored) {
            return replaceMarkup(markup);
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

    private String replaceMarkup(String text) {
        String formatted = replaceInline(text, ITALIC, "*$1*");
        formatted = replaceInline(formatted, BOLD, "**$1**");

        return replaceSiteLinks(formatted);
    }

    private String replaceSiteLinks(String text) {
        Matcher matcher = GLOSSARY_LINK.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String label = matcher.group(1).trim();
            String url = matcher.group(2).trim();
            String replacement = "[" + label + "](" + siteUrl() + "/glossary/" + url + ")";
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
}
