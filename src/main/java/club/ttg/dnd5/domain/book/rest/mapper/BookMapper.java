package club.ttg.dnd5.domain.book.rest.mapper;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.rest.dto.BookDetailResponse;
import club.ttg.dnd5.domain.book.rest.dto.BookRequest;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {
        BaseMapping.class
})
public interface BookMapper {
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = "acronym", target = "name.label")
    ShortResponse toShort(Book book);

    @BaseMapping.BaseShortResponseNameMapping
    BookDetailResponse toDetail(Book book);

    @BaseMapping.BaseEntityNameMapping
    Book toEntity(BookRequest request);

    @BaseMapping.BaseEntityNameMapping
    void toEntity(BookRequest request, @MappingTarget Book book);
}
