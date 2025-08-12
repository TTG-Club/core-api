package club.ttg.dnd5.domain.articles.repository;

import club.ttg.dnd5.domain.articles.model.Article;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
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
}
