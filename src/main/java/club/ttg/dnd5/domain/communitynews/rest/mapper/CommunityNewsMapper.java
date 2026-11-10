package club.ttg.dnd5.domain.communitynews.rest.mapper;

import club.ttg.dnd5.domain.common.validation.VideoUrls;
import club.ttg.dnd5.domain.communitynews.model.CommunityNews;
import club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsDetailedResponse;
import club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsRequest;
import club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;

/**
 * Пустые строки необязательных полей (автор, ссылки) храним как {@code null}; в форме для правки
 * ({@link #toRequest}) пустые автор и тексты отдаются {@code ""}.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = "spring")
public interface CommunityNewsMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "draft", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "publishDateTime", ignore = true)
    @Mapping(target = "authorName", source = "authorName", qualifiedByName = "blankToNull")
    @Mapping(target = "sourceUrl", source = "sourceUrl", qualifiedByName = "blankToNull")
    @Mapping(target = "previewImageUrl", source = "previewImageUrl", qualifiedByName = "blankToNull")
    @Mapping(target = "videoUrl", source = "videoUrl", qualifiedByName = "blankToNull")
    @BaseMapping.TimestampedMappingIgnore
    CommunityNews toEntity(CommunityNewsRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "draft", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "publishDateTime", ignore = true)
    @Mapping(target = "authorName", source = "authorName", qualifiedByName = "blankToNull")
    @Mapping(target = "sourceUrl", source = "sourceUrl", qualifiedByName = "blankToNull")
    @Mapping(target = "previewImageUrl", source = "previewImageUrl", qualifiedByName = "blankToNull")
    @Mapping(target = "videoUrl", source = "videoUrl", qualifiedByName = "blankToNull")
    @BaseMapping.TimestampedMappingIgnore
    void updateEntity(@MappingTarget CommunityNews news, CommunityNewsRequest request);

    @Named("short")
    @Mapping(source = "status.name", target = "statusName")
    CommunityNewsShortResponse toShortResponse(CommunityNews news);

    // Полный ответ — наследник краткого, поэтому элемент списка указываем явно.
    @IterableMapping(qualifiedByName = "short")
    List<CommunityNewsShortResponse> toShortResponseList(Collection<CommunityNews> news);

    @Mapping(source = "status.name", target = "statusName")
    CommunityNewsDetailedResponse toDetailedResponse(CommunityNews news);

    @Mapping(target = "authorName", source = "authorName", qualifiedByName = "nullToEmpty")
    @Mapping(target = "preview", source = "preview", qualifiedByName = "nullToEmpty")
    @Mapping(target = "content", source = "content", qualifiedByName = "nullToEmpty")
    CommunityNewsRequest toRequest(CommunityNews news);

    @Named("blankToNull")
    default String blankToNull(String value) {
        return VideoUrls.normalize(value);
    }

    @Named("nullToEmpty")
    default String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
