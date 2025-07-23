package club.ttg.dnd5.domain.articles.repository;

import club.ttg.dnd5.domain.articles.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArticleFilterRepository extends JpaRepository<Article, Long> {
    //дописать запрос квери для тегов
    @Query("SELECT DISTINCT a.categories FROM Articles a WHERE a.categories IS NOT NULL")
    List<String> findDistinctTagCategories();
}
