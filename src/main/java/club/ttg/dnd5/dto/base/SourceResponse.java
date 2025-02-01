package club.ttg.dnd5.dto.base;


import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.model.book.Source;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName("source")
public class SourceResponse implements GroupStrategy {
    private NameBasedDTO name = new NameBasedDTO();
    private NameBasedDTO group = new NameBasedDTO();
    private int page;
    private boolean homebrew = false;
    private boolean thirdParty = false;

    @Override
    public void determineGroup(Source source) {
        Book bookInfo = source.getBookInfo();
        if (bookInfo != null) {
            group.setName("Официальные источники");
            group.setShortName("Basic");
        }
    }
}