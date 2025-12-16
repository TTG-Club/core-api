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
    @BaseMapping.TimestampedMappingIgnore
    Article toEntity(ArticleRequest article);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @BaseMapping.TimestampedMappingIgnore
    void updateEntity(@MappingTarget Article article, ArticleRequest articleRequest);

    ArticleDetailedResponse toDetailedResponse(Article byUrl);

    ArticleRequest toRequest(Article article);

    ArticleShortResponse toShortResponse(Article article);

    List<ArticleShortResponse> toShortResponseList(Collection<Article> articles);

}
