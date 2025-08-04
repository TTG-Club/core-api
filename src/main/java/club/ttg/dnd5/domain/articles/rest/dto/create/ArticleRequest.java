package club.ttg.dnd5.domain.articles.rest.dto.create;

import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleRequest extends BaseRequest {
    private String categories;
    private Collection<String> tagsArticles;
}
