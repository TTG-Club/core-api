package club.ttg.dnd5.domain.article.repository;

import club.ttg.dnd5.domain.article.model.Article;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {

    boolean existsByUrl(String url);

    Optional<Article> findByUrl(String url);

    List<Article> findAllByDeletedFalseOrderByCreatedAtDesc(Limit limit);

}
