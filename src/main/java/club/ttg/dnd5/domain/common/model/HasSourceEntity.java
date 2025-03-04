package club.ttg.dnd5.domain.common.model;

import club.ttg.dnd5.domain.book.model.Source;
@Deprecated
public interface HasSourceEntity {
    Source getSource();
    void setSource(Source source);
}
