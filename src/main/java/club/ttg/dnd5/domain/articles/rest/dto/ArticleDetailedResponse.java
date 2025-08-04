package club.ttg.dnd5.domain.articles.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ArticleDetailedResponse extends BaseResponse {
    private String categories;
    private Collection<String> tagsArticles;
}
