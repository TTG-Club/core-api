package club.ttg.dnd5.domain.species.repository;

import club.ttg.dnd5.domain.species.model.SpeciesFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpeciesFeatureRepository extends JpaRepository<SpeciesFeature, String> {
}
