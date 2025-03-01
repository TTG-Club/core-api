package club.ttg.dnd5.domain.common;

import club.ttg.dnd5.domain.book.model.Source;

public interface GroupStrategy {
    void determineGroup(Source source);
}
