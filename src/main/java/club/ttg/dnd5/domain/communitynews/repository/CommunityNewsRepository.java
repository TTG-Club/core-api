package club.ttg.dnd5.domain.communitynews.repository;

import club.ttg.dnd5.domain.communitynews.model.CommunityNews;
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
public interface CommunityNewsRepository extends JpaRepository<CommunityNews, UUID> {

    /** Занят ли url — в том числе черновиком или удалённой записью. */
    boolean existsByUrl(String url);

    Optional<CommunityNews> findByUrl(String url);

    /**
     * Публичная выдача одной записи по url: не удалена, не черновик И (в общем доступе — активна
     * и дата наступила — ЛИБО открыта по прямой ссылке).
     */
    @Query("""
            SELECT n FROM CommunityNews n
            WHERE n.url = :url
              AND n.deleted = false
              AND n.draft = false
              AND (
                (n.active = true AND n.publishDateTime < :now)
                OR n.accessibleByLink = true
              )
            """)
    Optional<CommunityNews> findAccessibleByUrl(@Param("url") String url, @Param("now") Instant now);

    /**
     * Живые записи (публичная лента): не удалены, не черновик, активны, дата выхода наступила.
     * {@code pattern} — готовый LIKE-шаблон в нижнем регистре ("%текст%" или "%" без фильтра).
     */
    @Query("""
            SELECT n FROM CommunityNews n
            WHERE n.deleted = false
              AND n.draft = false
              AND n.active = true
              AND n.publishDateTime < :now
              AND LOWER(n.title) LIKE :pattern
            ORDER BY n.publishDateTime DESC
            """)
    List<CommunityNews> findPublished(@Param("now") Instant now, @Param("pattern") String pattern, Limit limit);

    /** Не-живые записи (черновики + снятые + запланированные) для редакторов. */
    @Query("""
            SELECT n FROM CommunityNews n
            WHERE n.deleted = false
              AND (
                n.draft = true
                OR n.active = false
                OR n.publishDateTime IS NULL
                OR n.publishDateTime >= :now
              )
              AND LOWER(n.title) LIKE :pattern
            ORDER BY n.createdAt DESC
            """)
    List<CommunityNews> findUnpublished(@Param("now") Instant now, @Param("pattern") String pattern, Limit limit);
}
