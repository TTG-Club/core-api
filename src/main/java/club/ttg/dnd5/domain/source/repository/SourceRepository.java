package club.ttg.dnd5.domain.source.repository;

import club.ttg.dnd5.domain.source.model.Source;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;

public interface SourceRepository extends JpaRepository<Source, String> {
    @Query(value = """
            select s from Source s
            where replace(replace(s.name, 'Ё', 'Е'), 'ё', 'е') ilike concat('%', :searchLine, '%')
               or s.english ilike concat('%', :searchLine, '%')
               or replace(replace(s.alternative, 'Ё', 'Е'), 'ё', 'е') ilike concat('%', :searchLine, '%')
               or s.acronym ilike concat('%', :searchLine, '%')
               or replace(replace(s.name, 'Ё', 'Е'), 'ё', 'е') ilike concat('%', :invertedSearchLine, '%')
               or s.english ilike concat('%', :invertedSearchLine, '%')
               or replace(replace(s.alternative, 'Ё', 'Е'), 'ё', 'е') ilike concat('%', :invertedSearchLine, '%')
               or s.acronym ilike concat('%', :invertedSearchLine, '%')
            """
    )
    Collection<Source> findBySearchLine(String searchLine, String invertedSearchLine, Sort defaultSort);

    Optional<Source> findByUrl(String url);

    @Query("select s.acronym from Source s where s.url = :url")
    Optional<String> findAcronymByUrl(String url);

    boolean existsByUrl(String url);

}
