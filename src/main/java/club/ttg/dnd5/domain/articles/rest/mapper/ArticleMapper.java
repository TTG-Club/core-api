package club.ttg.dnd5.domain.articles.rest.mapper;


import club.ttg.dnd5.domain.articles.model.Article;
import club.ttg.dnd5.domain.articles.rest.dto.ArticleDetailedResponse;
import club.ttg.dnd5.domain.articles.rest.dto.ArticleShortResponse;
import club.ttg.dnd5.domain.articles.rest.dto.create.ArticleRequest;
import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Mapper(componentModel = "spring")
public interface ArticleMapper {
    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(target = "categories", source = "categories", qualifiedByName = "capitalize")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "capitalize")
    ArticleDetailedResponse toDetail(Article article);


    @BaseMapping.BaseRequestNameMapping
    @Mapping(source = "source.url", target = "source.url")
    @Mapping(source = "sourcePage", target = "source.page")
    ArticleRequest toRequest(Article article);

    @ArticleMapper.ToEntityMapping
    Article toEntity(ArticleRequest request, Book source);

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(target = "categories", source = "categories", qualifiedByName = "capitalize")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "capitalize")
    ArticleShortResponse toShort(Article article);

    @ArticleMapper.ToEntityMapping
    void updateEntity(@MappingTarget Article article, ArticleRequest request);

    @Named("capitalize")
    default String capitalize(String string) {
        return StringUtils.capitalize(string);
    }

    @Named("collectToString")
    default String collectToString(Collection<String> names) {
        return String.join(" ", names);
    }

    @Retention(RetentionPolicy.SOURCE)
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    @BaseMapping.BaseEntityNameMapping
    @interface ToEntityMapping {
    }

    @Named("altToCollection")
    default Collection<String> altToCollection(String string) {
        if(org.apache.commons.lang3.StringUtils.isEmpty(string)) {
            return Collections.emptyList();
        }
        return Arrays.asList(string.split(";"));
    }
}
