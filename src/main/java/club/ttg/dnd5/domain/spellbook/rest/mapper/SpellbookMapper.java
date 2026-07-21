package club.ttg.dnd5.domain.spellbook.rest.mapper;

import club.ttg.dnd5.domain.spellbook.model.Spellbook;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookDetailedResponse;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookLevelGroupResponse;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookShortResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Ответы владельца и читателя различаются ключом ссылки: {@code shareKey} отдаётся только
 * владельцу — иначе получивший книгу по ссылке мог бы раздавать её дальше.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = "spring")
public interface SpellbookMapper {

    @Mapping(source = "spellbook.id", target = "id")
    @Mapping(source = "spellbook.name", target = "name")
    @Mapping(source = "spellbook.ownerUsername", target = "ownerUsername")
    @Mapping(source = "spellbook.shareKey", target = "shareKey")
    @Mapping(source = "spellbook.createdAt", target = "createdAt")
    @Mapping(source = "spellbook.updatedAt", target = "updatedAt")
    @Mapping(source = "spellCount", target = "spellCount")
    @Mapping(source = "preparedCount", target = "preparedCount")
    SpellbookShortResponse toOwnShortResponse(Spellbook spellbook, long spellCount, long preparedCount);

    @Mapping(source = "spellbook.id", target = "id")
    @Mapping(source = "spellbook.name", target = "name")
    @Mapping(source = "spellbook.ownerUsername", target = "ownerUsername")
    @Mapping(target = "shareKey", ignore = true)
    @Mapping(source = "spellbook.createdAt", target = "createdAt")
    @Mapping(source = "spellbook.updatedAt", target = "updatedAt")
    @Mapping(source = "spellCount", target = "spellCount")
    @Mapping(source = "preparedCount", target = "preparedCount")
    SpellbookShortResponse toSharedShortResponse(Spellbook spellbook, long spellCount, long preparedCount);

    @Mapping(source = "spellbook.id", target = "id")
    @Mapping(source = "spellbook.name", target = "name")
    @Mapping(source = "spellbook.ownerUsername", target = "ownerUsername")
    @Mapping(source = "spellbook.shareKey", target = "shareKey")
    @Mapping(target = "owner", constant = "true")
    @Mapping(source = "spellbook.createdAt", target = "createdAt")
    @Mapping(source = "spellbook.updatedAt", target = "updatedAt")
    @Mapping(source = "levels", target = "levels")
    @Mapping(source = "spellCount", target = "spellCount")
    @Mapping(source = "preparedCount", target = "preparedCount")
    SpellbookDetailedResponse toOwnDetailedResponse(Spellbook spellbook,
                                                    List<SpellbookLevelGroupResponse> levels,
                                                    long spellCount,
                                                    long preparedCount);

    @Mapping(source = "spellbook.id", target = "id")
    @Mapping(source = "spellbook.name", target = "name")
    @Mapping(source = "spellbook.ownerUsername", target = "ownerUsername")
    @Mapping(target = "shareKey", ignore = true)
    @Mapping(target = "owner", constant = "false")
    @Mapping(source = "spellbook.createdAt", target = "createdAt")
    @Mapping(source = "spellbook.updatedAt", target = "updatedAt")
    @Mapping(source = "levels", target = "levels")
    @Mapping(source = "spellCount", target = "spellCount")
    @Mapping(source = "preparedCount", target = "preparedCount")
    SpellbookDetailedResponse toSharedDetailedResponse(Spellbook spellbook,
                                                       List<SpellbookLevelGroupResponse> levels,
                                                       long spellCount,
                                                       long preparedCount);
}
