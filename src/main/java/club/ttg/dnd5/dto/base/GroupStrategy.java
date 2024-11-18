package club.ttg.dnd5.dto.base;

import club.ttg.dnd5.model.book.Source;

public interface GroupStrategy {
    void determineGroup(Source source);
}
