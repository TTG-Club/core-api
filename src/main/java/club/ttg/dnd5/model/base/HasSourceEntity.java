package club.ttg.dnd5.model.base;

import club.ttg.dnd5.model.Source;

public interface HasSourceEntity {
    Source getSource();
    Short getPage();
    void setPage(Short page);
    void setSource(String sourceName);
    void setSource(Source source);
}
