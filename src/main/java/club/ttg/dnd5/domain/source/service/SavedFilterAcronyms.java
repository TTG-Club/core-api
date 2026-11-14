package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceGroupFilter;

import java.util.Objects;

/**
 * Замена акронима источника в сохранённом фильтре пользователя.
 */
final class SavedFilterAcronyms {
    private SavedFilterAcronyms() {
    }

    /**
     * Меняет акроним в группах источников, не трогая отметку пользователя (включён,
     * выключен, не выбран): иначе переименованная книга стала бы для него «новой» и
     * включилась сама.
     *
     * @return {@code true}, если фильтр изменился
     */
    static boolean rename(FilterInfo filter, String oldAcronym, String newAcronym) {
        if (filter == null || filter.getGroups() == null) {
            return false;
        }
        boolean changed = false;
        for (Object group : filter.getGroups()) {
            if (!(group instanceof SourceGroupFilter sources) || sources.getFilters() == null) {
                continue;
            }
            for (SourceGroupFilter.SourceFilterItem item : sources.getFilters()) {
                if (item != null && Objects.equals(item.getValue(), oldAcronym)) {
                    item.setValue(newAcronym);
                    changed = true;
                }
            }
        }
        return changed;
    }
}
