package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgGlossary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Маппер записи глоссария TTG Club в формат компендиума VTTG ({@code type = "glossary"}).
 *
 * <p>Запись справочная и плоская: идентичность, категория тега и текст описания. Разметка
 * описания разбирается тем же {@link VttgMarkupConverter}, что и у остальных разделов —
 * термины ссылаются друг на друга и на прочие разделы сайта.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgGlossaryMapper {
    private static final String TYPE = "glossary";
    private static final String TYPE_LABEL = "Глоссарий";
    /** Слаг листа дерева разделов для глоссария (см. {@link VttgCompendiumSections}). */
    private static final String SECTION = "glossary";
    /**
     * Категория записей без тега. Пустой категории быть не должно: по {@code category} идёт
     * группировка списка в VTTG, и записи без неё сложились бы в безымянную группу.
     */
    private static final String DEFAULT_CATEGORY = "Прочее";

    private final VttgMarkupConverter markupConverter;

    public VttgGlossary toVttg(Glossary glossary) {
        return VttgGlossary.builder()
                .id(slug(glossary.getUrl()))
                .name(glossary.getName())
                .nameEn(optional(glossary.getEnglish()))
                .type(TYPE)
                .section(SECTION)
                .srcSection(SectionType.GLOSSARY.getValue())
                .srcUrl(glossary.getUrl())
                .sourceKey(VttgSourceKeys.of(glossary.getSource()))
                .isSRD(glossary.getSrdVersion() != null)
                .category(category(glossary.getTagCategory()))
                .description(markupConverter.toText(glossary.getDescription()))
                .typeLabel(TYPE_LABEL)
                .build();
    }

    private String category(String tagCategory) {
        return StringUtils.hasText(tagCategory)
                ? StringUtils.capitalize(tagCategory.trim())
                : DEFAULT_CATEGORY;
    }

    private String slug(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String optional(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}
