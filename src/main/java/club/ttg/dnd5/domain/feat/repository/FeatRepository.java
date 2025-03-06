package club.ttg.dnd5.domain.feat.repository;

import club.ttg.dnd5.domain.feat.model.Feat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeatRepository extends JpaRepository<Feat, String>,
        JpaSpecificationExecutor<Feat> {
    Optional<Feat> findByName(String feat);
}
