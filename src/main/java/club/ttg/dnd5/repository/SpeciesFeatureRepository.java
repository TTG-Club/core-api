package club.ttg.dnd5.repository;

import club.ttg.dnd5.model.species.SpeciesFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpeciesFeatureRepository extends JpaRepository<SpeciesFeature, String> {
}
