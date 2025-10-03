package club.ttg.dnd5.domain.species.repository;

import club.ttg.dnd5.domain.species.model.Species;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface SpeciesRepository extends JpaRepository<Species, String> {
    Collection<Species> findByParent(Species parent);

    Collection<Species> findAllByParentIsNull(final Sort by);

    @Query(value = """
        select s from Species s
        where s.parent is not null
        order by s.parent.name, s.name
        """)
    Collection<Species> findAllByParentIsNotNull();

    @Query(value = """
            SELECT s FROM Species s
            WHERE s.name ILIKE concat('%', :searchLine, '%')
               OR s.english ILIKE concat('%', :searchLine, '%')
               OR s.alternative ILIKE concat('%', :searchLine, '%')
               OR s.name ILIKE CONCAT('%', :invertedSearchLine, '%')
               OR s.english ILIKE CONCAT('%', :invertedSearchLine, '%')
               OR s.alternative ILIKE CONCAT('%', :invertedSearchLine, '%')
            """
    )
    Collection<Species> findAllSearch(String searchLine, String invertedSearchLine, Sort by);

    Integer countByUsername(String username);
}
