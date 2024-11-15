package club.ttg.dnd5.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface HasSourceDTO {
    String getSource();
    Short getPage();
    void setPage(Short page);
    void setSource(String source);
}
