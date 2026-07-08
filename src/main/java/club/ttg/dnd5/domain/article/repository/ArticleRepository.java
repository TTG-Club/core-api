package club.ttg.dnd5.domain.article.repository;

import club.ttg.dnd5.domain.article.model.Article;
import club.ttg.dnd5.domain.article.model.ArticleType;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {

    boolean existsByUrl(String url);

    Optional<Article> findByUrl(String url);

    /**
     * Публичная выдача одной записи по url: доступна, если запись не удалена,
     * не черновик И (уже в общем доступе — активна и дата наступила — ЛИБО открыта
     * по прямой ссылке).
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.url = :url
              AND a.deleted = false
              AND a.draft = false
              AND (
                (a.active = true AND a.publishDateTime < :now)
                OR a.accessibleByLink = true
              )
            """)
    Optional<Article> findAccessibleByUrl(@Param("url") String url, @Param("now") Instant now);

    /**
     * Опубликованные и активные записи (публичная лента). Опциональный фильтр по типу —
     * инлайном `(:type is null or ...)`, как в остальных репозиториях проекта.
     * Параметр pattern — готовый LIKE-шаблон в нижнем регистре ("%текст%" или "%" без фильтра);
     * он собирается в сервисе, чтобы не давать Hibernate вывести тип строкового параметра как bytea.
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.deleted = false
              AND a.draft = false
              AND a.active = true
              AND a.publishDateTime < :now
              AND (:type IS NULL OR a.type = :type)
              AND LOWER(a.title) LIKE :pattern
            ORDER BY a.publishDateTime DESC
            """)
    List<Article> findPublished(@Param("now") Instant now, @Param("type") ArticleType type,
                                @Param("pattern") String pattern, Limit limit);

    /**
     * Не-живые записи (черновики + запланированные + неактивные) для модерации.
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.deleted = false
              AND (
                a.draft = true
                OR a.active = false
                OR a.publishDateTime IS NULL
                OR a.publishDateTime >= :now
              )
              AND (:type IS NULL OR a.type = :type)
              AND LOWER(a.title) LIKE :pattern
            ORDER BY a.createdAt DESC
            """)
    List<Article> findUnpublished(@Param("now") Instant now, @Param("type") ArticleType type,
                                  @Param("pattern") String pattern, Limit limit);
}
