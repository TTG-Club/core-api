package club.ttg.dnd5.domain.common.url;

import club.ttg.dnd5.domain.common.model.SectionType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Таблицы разделов каталога, у записей которых адрес ({@code url}) — первичный ключ, и
 * разделы сайта, которым они соответствуют.
 */
public final class CatalogTables {
    /** Таблица → раздел (значение {@link SectionType}, оно же первый сегмент пути API), в постоянном порядке. */
    public static final Map<String, String> SECTIONS;

    static {
        Map<String, String> sections = new LinkedHashMap<>();
        sections.put("spell", SectionType.SPELL.getValue());
        sections.put("bestiary", SectionType.BESTIARY.getValue());
        sections.put("class", SectionType.CLASS.getValue());
        sections.put("species", SectionType.SPECIES.getValue());
        sections.put("feat", SectionType.FEAT.getValue());
        sections.put("background", SectionType.BACKGROUND.getValue());
        sections.put("item", SectionType.ITEM.getValue());
        sections.put("magic_item", SectionType.MAGIC_ITEM.getValue());
        sections.put("glossary", SectionType.GLOSSARY.getValue());
        sections.put("bastion_facility", SectionType.BASTION.getValue());
        SECTIONS = Collections.unmodifiableMap(sections);
    }

    private CatalogTables() {
    }

    /** Раздел таблицы каталога; пусто, если таблица не из каталога. */
    public static Optional<String> section(String table) {
        return Optional.ofNullable(SECTIONS.get(table));
    }
}
