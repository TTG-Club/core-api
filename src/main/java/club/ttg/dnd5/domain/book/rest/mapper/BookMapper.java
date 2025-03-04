package club.ttg.dnd5.domain.book.rest.mapper;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.rest.dto.BookDetailResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {
    @Mapping(source = "name", target = "name.name")
    BookDetailResponse toDetailResponse(Book book);
}
