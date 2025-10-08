package club.ttg.dnd5.domain.book.rest.dto;

import club.ttg.dnd5.domain.book.model.TypeBook;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookRequest extends BaseRequest {
    private String acronym;
    @Schema(description = "тип источника")
    private TypeBook type;
    @Schema(description = "дата выхода книги")
    private String published;
    private String image;
}
