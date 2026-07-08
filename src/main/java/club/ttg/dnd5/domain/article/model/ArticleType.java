package club.ttg.dnd5.domain.article.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleType {
    NEWS("Новость"),
    ARTICLE("Статья");

    private final String name;
}
