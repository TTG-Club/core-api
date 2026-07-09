package club.ttg.dnd5.domain.article.rest.mapper;

import club.ttg.dnd5.domain.article.model.Article;
import club.ttg.dnd5.domain.article.rest.dto.ArticleDetailedResponse;
import club.ttg.dnd5.domain.article.rest.dto.ArticleRequest;
import club.ttg.dnd5.domain.article.rest.dto.ArticleShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = "spring")
public interface ArticleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "draft", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "publishDateTime", ignore = true)
    @Mapping(target = "telegramPostedAt", ignore = true)
    @Mapping(target = "telegramMessageId", ignore = true)
    @Mapping(target = "telegramPhoto", ignore = true)
    @Mapping(target = "telegramDirty", ignore = true)
    @Mapping(target = "discordPostedAt", ignore = true)
    @Mapping(target = "discordMessageId", ignore = true)
    @Mapping(target = "discordDirty", ignore = true)
    @Mapping(target = "vkPostedAt", ignore = true)
    @Mapping(target = "vkPostId", ignore = true)
    @Mapping(target = "vkAttachment", ignore = true)
    @Mapping(target = "vkDirty", ignore = true)
    @BaseMapping.TimestampedMappingIgnore
    Article toEntity(ArticleRequest article);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "draft", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "publishDateTime", ignore = true)
    @Mapping(target = "telegramPostedAt", ignore = true)
    @Mapping(target = "telegramMessageId", ignore = true)
    @Mapping(target = "telegramPhoto", ignore = true)
    @Mapping(target = "telegramDirty", ignore = true)
    @Mapping(target = "discordPostedAt", ignore = true)
    @Mapping(target = "discordMessageId", ignore = true)
    @Mapping(target = "discordDirty", ignore = true)
    @Mapping(target = "vkPostedAt", ignore = true)
    @Mapping(target = "vkPostId", ignore = true)
    @Mapping(target = "vkAttachment", ignore = true)
    @Mapping(target = "vkDirty", ignore = true)
    @BaseMapping.TimestampedMappingIgnore
    void updateEntity(@MappingTarget Article article, ArticleRequest articleRequest);

    @Mapping(source = "type.name", target = "typeName")
    @Mapping(source = "status.name", target = "statusName")
    ArticleDetailedResponse toDetailedResponse(Article byUrl);

    ArticleRequest toRequest(Article article);

    @Mapping(source = "type.name", target = "typeName")
    @Mapping(source = "status.name", target = "statusName")
    ArticleShortResponse toShortResponse(Article article);

    List<ArticleShortResponse> toShortResponseList(Collection<Article> articles);

}
