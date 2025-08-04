package club.ttg.dnd5.domain.articles.repository;

import club.ttg.dnd5.domain.articles.model.Article;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, String> {
    @Query(value = """
            select a from Article a
            where a.name ilike concat('%', :searchLine, '%')
               or a.english ilike concat('%', :searchLine, '%')
               or a.alternative ilike concat('%', :searchLine, '%')
               or a.english ilike concat('%', :invertedSearchLine, '%')
               or a.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    List<Article> findBySearchLine(String searchLine, String invertedSearchLine, Sort sort);

    @Query(value = """
    select distinct jsonb_array_elements_text(tagsArticles) as tag
    from articles
    where tagsArticles is not null
    """, nativeQuery = true)
    List<String> findAllUniquetagsArticles();

    @Query(value = """
    select distinct jsonb_array_elements_text(categories::jsonb) as category
    from articles
    where categories is not null
    """, nativeQuery = true)
    List<String> findAllUniqueCategories();
}
