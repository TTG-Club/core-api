package club.ttg.dnd5.repository;

import club.ttg.dnd5.model.species.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpeciesRepository extends JpaRepository<Species, String>, JpaSpecificationExecutor<Species> {
    List<Species> findByParent(Species parent);
    Optional<Species> findByNameIgnoreCase(String name);
}
