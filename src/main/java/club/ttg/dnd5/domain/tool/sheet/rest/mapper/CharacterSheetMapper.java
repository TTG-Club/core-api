package club.ttg.dnd5.domain.tool.sheet.rest.mapper;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetResponse;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = "spring")
public interface CharacterSheetMapper {

    CharacterSheetResponse toResponse(CharacterSheet sheet);

    /**
     * Элемент списка: у удалённых листов документ не отдаётся (история показывает только
     * название и даты, а полный JSON вернётся после восстановления).
     */
    @Named("listItem")
    @Mapping(target = "data", expression = "java(sheet.isDeleted() ? null : sheet.getData())")
    CharacterSheetResponse toListItemResponse(CharacterSheet sheet);

    @IterableMapping(qualifiedByName = "listItem")
    List<CharacterSheetResponse> toListItemResponseList(Collection<CharacterSheet> sheets);
}
