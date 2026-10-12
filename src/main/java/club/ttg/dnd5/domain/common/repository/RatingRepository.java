package club.ttg.dnd5.domain.common.repository;

import club.ttg.dnd5.domain.common.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String> {
    @Query("SELECT AVG(r.value) AS value, COUNT(r.value) AS total FROM Rating r WHERE r.section = :section AND r.url=:url")
    RatingStats getRating(String section, String url);

    /**
     * Рейтинги сразу нескольких записей раздела — одним запросом для списков. Записей без оценок в выдаче нет.
     */
    @Query("""
            SELECT r.url AS url, AVG(r.value) AS value, COUNT(r.value) AS total FROM Rating r
            WHERE r.section = :section AND r.url IN :urls
            GROUP BY r.url
            """)
    List<UrlRatingStats> getRatings(String section, Collection<String> urls);

    Optional<Rating> findByUsernameAndSectionAndUrl(String username, String section, String url);

    Long countByUsername(String username);
}
