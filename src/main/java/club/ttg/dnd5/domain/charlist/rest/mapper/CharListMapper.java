package club.ttg.dnd5.domain.charlist.rest.mapper;

import club.ttg.dnd5.domain.charlist.model.CharList;
import club.ttg.dnd5.domain.charlist.rest.dto.CharListDetailedResponse;
import club.ttg.dnd5.domain.charlist.rest.dto.CharListRequest;
import club.ttg.dnd5.domain.charlist.rest.dto.CharListShortResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CharListMapper {
    CharListShortResponse toShort(CharList charList);
    CharListDetailedResponse toDetailed(CharList charList);
    CharList toEntity(CharListRequest request);
}
