package club.ttg.dnd5.model.base;

import club.ttg.dnd5.model.book.Source;

public interface HasSourceEntity {
    Source getSource();
    void setSource(Source source);
}
