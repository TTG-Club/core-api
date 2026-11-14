package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SourceFilterInfo;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceGroupFilter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavedFilterAcronymsTest {

    /** Выключенная пользователем книга после смены акронима остаётся выключенной. */
    @Test
    void disabledSourceStaysDisabled() {
        FilterInfo filter = filter(item("XGE", false), item("PHB", true));

        assertTrue(SavedFilterAcronyms.rename(filter, "XGE", "XGTE"));

        assertEquals(selection("XGTE", false, "PHB", true), selection(filter));
    }

    /** Включённая остаётся включённой, а не становится «новой книгой». */
    @Test
    void enabledSourceStaysEnabled() {
        FilterInfo filter = filter(item("XGE", true), item("PHB", false));

        assertTrue(SavedFilterAcronyms.rename(filter, "XGE", "XGTE"));

        assertEquals(selection("XGTE", true, "PHB", false), selection(filter));
    }

    /** Отметка «не выбрано» (null) тоже сохраняется. */
    @Test
    void unselectedSourceStaysUnselected() {
        SourceGroupFilter.SourceFilterItem item = item("XGE", true);
        item.setSelected(null);
        FilterInfo filter = filter(item);

        assertTrue(SavedFilterAcronyms.rename(filter, "XGE", "XGTE"));

        assertEquals(selection("XGTE", null), selection(filter));
    }

    /** Регистр учитывается: фильтр с другим акронимом не меняется. */
    @Test
    void otherAcronymIsNotTouched() {
        FilterInfo filter = filter(item("xge", true));

        assertFalse(SavedFilterAcronyms.rename(filter, "XGE", "XGTE"));
        assertFalse(SavedFilterAcronyms.rename(null, "XGE", "XGTE"));

        assertEquals(selection("xge", true), selection(filter));
    }

    private static FilterInfo filter(SourceGroupFilter.SourceFilterItem... items) {
        return new SourceFilterInfo(List.of(new SourceGroupFilter(List.of(items), "Базовые")));
    }

    private static SourceGroupFilter.SourceFilterItem item(String acronym, boolean selected) {
        return new SourceGroupFilter.SourceFilterItem(acronym, acronym, selected);
    }

    private static Map<String, Boolean> selection(Object... pairs) {
        Map<String, Boolean> selection = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            selection.put((String) pairs[i], (Boolean) pairs[i + 1]);
        }
        return selection;
    }

    private static Map<String, Boolean> selection(FilterInfo filter) {
        Map<String, Boolean> selection = new HashMap<>();
        filter.getGroups().stream()
                .map(SourceGroupFilter.class::cast)
                .flatMap(group -> group.getFilters().stream())
                .forEach(item -> selection.put(item.getValue(), item.getSelected()));
        return selection;
    }
}
