package club.ttg.dnd5.domain.spell.repository;

import club.ttg.dnd5.domain.spell.model.Spell;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SpellRepository extends JpaRepository<Spell, String> {
    @EntityGraph(attributePaths = {
            "source",
            "classAffiliation", "classAffiliation.source",
            "subclassAffiliation", "subclassAffiliation.source",
            "speciesAffiliation", "speciesAffiliation.source",
            "lineagesAffiliation", "lineagesAffiliation.source",
            "featAffiliation", "featAffiliation.source"
    })
    @Query("select s from Spell s where s.url = :url")
    Optional<Spell> findDetailedByUrl(String url);

    @EntityGraph(attributePaths = {
            "source",
            "classAffiliation",
            "subclassAffiliation",
            "speciesAffiliation",
            "lineagesAffiliation",
            "featAffiliation"
    })
    @Query("select s from Spell s where s.url = :url")
    Optional<Spell> findFormByUrl(String url);

    @Query(value = """
            select s from Spell s
            where s.name ilike concat('%', :searchLine, '%')
               or s.english ilike concat('%', :searchLine, '%')
               or s.alternative ilike concat('%', :searchLine, '%')
               or s.name ilike concat('%', :invertedSearchLine, '%')
               or s.english ilike concat('%', :invertedSearchLine, '%')
               or s.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    List<Spell> findBySearchLine(String searchLine, String invertedSearchLine, Sort sort);

    @Query(value = """
        select distinct s.source
        from spell s
        where s.source is not null
        order by s.source
        """, nativeQuery = true)
    List<String> findAllUsedSourceCodes();

}
