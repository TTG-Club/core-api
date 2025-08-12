package club.ttg.dnd5.domain.articles.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleShortResponse extends ShortResponse{
    private String categories;

    @JsonProperty("tags_articles")
    private Collection<String> tagsArticles;
}
