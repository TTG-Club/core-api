package club.ttg.dnd5.domain.species.repository;

import club.ttg.dnd5.domain.species.model.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SpeciesRepository extends JpaRepository<Species, String> {
    Collection<Species> findByParent(Species parent);

    @Query(value = """
        select s from Species s
        where s.parent is not null
        order by s.parent.name, s.name
        """)
    Collection<Species> findAllByParentIsNotNull();

    @Query(value = """
        select distinct s.source
        from species s
        where s.source is not null
        order by s.source
        """, nativeQuery = true)
    List<String> findAllUsedSourceCodes();
}
