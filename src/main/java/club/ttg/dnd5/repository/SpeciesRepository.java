package club.ttg.dnd5.repository;

import club.ttg.dnd5.model.species.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface SpeciesRepository extends JpaRepository<Species, String>, JpaSpecificationExecutor<Species> {
    Collection<Species> findByParent(Species parent);
    Optional<Species> findByNameIgnoreCase(String name);

    Collection<Species> findAllByParentIsNull();
}
