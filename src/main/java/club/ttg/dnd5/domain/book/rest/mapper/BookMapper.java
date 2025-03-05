package club.ttg.dnd5.domain.book.rest.mapper;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "englishName", target = "name.english")
    @Mapping(source = "sourceAcronym", target = "name.label")
    ShortResponse toShortResponse(Book book);
}
