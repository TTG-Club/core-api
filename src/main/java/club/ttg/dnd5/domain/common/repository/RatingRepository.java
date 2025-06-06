package club.ttg.dnd5.domain.common.repository;

import club.ttg.dnd5.domain.common.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String> {
    @Query("SELECT AVG(r.value) AS value, COUNT(r.value) AS total FROM Rating r WHERE r.section = :section AND r.url=:url")
    RatingStats getRating(String section, String url);

    Optional<Rating> findByUsernameAndSectionAndUrl(String username, String section, String url);
}
