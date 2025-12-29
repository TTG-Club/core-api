package club.ttg.dnd5.domain.update.rest.mapper;

import club.ttg.dnd5.domain.full_text_search.model.FullTextSearchView;
import club.ttg.dnd5.domain.update.rest.dto.ChangeAction;
import club.ttg.dnd5.domain.update.rest.dto.ChangeActionType;
import club.ttg.dnd5.domain.update.rest.dto.LastUpdate;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {BaseMapping.class})
public interface LastUpdateMapper {
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = "sourceName", target = "source.name.name")
    @Mapping(source = "sourceEnglish", target = "source.name.english")
    @Mapping(source = "acronym", target = "source.name.label")
    @Mapping(source = ".", target = "url", qualifiedByName = "buildUrl")
    @Mapping(source = ".", target = "action", qualifiedByName = "getAction")
    LastUpdate toResponse(FullTextSearchView request);

    @Named("buildUrl")
    default String buildUrl(FullTextSearchView request) {
        return "/" + request.getType().getValue() + "/" + request.getUrl();
    }

    @Named("getAction")
    default ChangeAction getAction(FullTextSearchView request) {
        if (request.getCreatedAt().equals(request.getUpdatedAt())) {
            return new ChangeAction(ChangeActionType.ADDED);
        } else {
            return new ChangeAction(ChangeActionType.UPDATED);
        }
    }
}
