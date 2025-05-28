package club.ttg.dnd5.domain.common.repository;

import club.ttg.dnd5.domain.common.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String> {
    @Query("SELECT AVG(r.value) FROM Rating r WHERE r.section = :section AND r.url=:url")
    Double getRating(String section, String url);
}
