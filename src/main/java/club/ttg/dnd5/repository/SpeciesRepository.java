package club.ttg.dnd5.repository;

import club.ttg.dnd5.model.species.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SpeciesRepository extends JpaRepository<Species, String>, JpaSpecificationExecutor<Species> {
}