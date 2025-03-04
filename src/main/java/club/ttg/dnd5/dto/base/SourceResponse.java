package club.ttg.dnd5.dto.base;


import club.ttg.dnd5.domain.common.GroupStrategy;
import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.model.Source;
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
public class SourceResponse {
    private NameResponse name = new NameResponse();
    private NameResponse group = new NameResponse();
    private int page;
}