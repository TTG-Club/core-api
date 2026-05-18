package club.ttg.dnd5.domain.species.repository;

import club.ttg.dnd5.domain.species.model.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpeciesRepository extends JpaRepository<Species, UUID> {

    Optional<Species> findByUrl(String url);

    boolean existsByUrl(String url);

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

    @Query(value = """
        select distinct s.srd_version
        from species s
        where s.srd_version is not null
        order by s.srd_version
        """, nativeQuery = true)
    List<String> findDistinctSrdVersions();
}
