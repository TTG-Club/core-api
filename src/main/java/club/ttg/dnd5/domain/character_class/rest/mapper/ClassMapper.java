package club.ttg.dnd5.domain.character_class.rest.mapper;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassDetailedResponse;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {BaseMapping.class}, componentModel = "spring")
public interface ClassMapper {

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "userId", source = "username")
    @Mapping(target = "gallery", ignore = true )
    ClassShortResponse toShortResponse(CharacterClass characterClass);

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "userId", source = "username")
    @Mapping(target = "gallery", ignore = true )
    ClassDetailedResponse toDetailedResponse(CharacterClass characterClass);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(target = "parent", source = "parent")
    @Mapping(target = "subclasses", ignore = true)

    @Mapping(target = "url", source = "request.url")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "imageUrl", source = "request.imageUrl")
    @Mapping(target = "hitDice", source = "request.hitDice")
    @Mapping(target = "savingThrows", source = "request.savingThrows")
    @Mapping(target = "features", source = "request.features")
    @Mapping(target = "table", source = "request.table")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "sourcePage", source = "request.source.page")
    CharacterClass toEntity(ClassRequest request, CharacterClass parent, Book source);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(target = "parent", source = "parent")
    @Mapping(target = "subclasses", ignore = true)

    @Mapping(target = "url", source = "request.url")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "imageUrl", source = "request.imageUrl")
    @Mapping(target = "hitDice", source = "request.hitDice")
    @Mapping(target = "savingThrows", source = "request.savingThrows")
    @Mapping(target = "features", source = "request.features")
    @Mapping(target = "table", source = "request.table")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "sourcePage", source = "request.source.page")
    CharacterClass updateEntity(@MappingTarget CharacterClass existingClass, CharacterClass parent, ClassRequest request, Book source);

    @BaseMapping.BaseRequestNameMapping
    @Mapping(target = "gallery", ignore = true)
    @Mapping(target = "parentUrl", source = "parent.url")
    @Mapping(source = "source.url", target = "source.url")
    @Mapping(source = "sourcePage", target = "source.page")
    ClassRequest toRequest(CharacterClass entity);

}
