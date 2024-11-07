package club.ttg.dnd5.dto.base;

public interface HasSourceDTO {
    String getSource();
    Short getPage();
    void setPage(Short page);
    void setSource(String source);
}
