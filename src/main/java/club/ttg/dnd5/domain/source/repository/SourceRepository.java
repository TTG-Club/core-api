package club.ttg.dnd5.domain.source.repository;

import club.ttg.dnd5.domain.source.model.Source;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;

public interface SourceRepository extends JpaRepository<Source, String> {
    @Query(value = """
            select f from Feat f
            where f.name ilike concat('%', :searchLine, '%')
               or f.english ilike concat('%', :searchLine, '%')
               or f.name ilike concat('%', :invertedSearchLine, '%')
               or f.english ilike concat('%', :invertedSearchLine, '%')
            """
    )
    Collection<Source> findBySearchLine(String searchLine, String invertedSearchLine, Sort defaultSort);
    Optional<Source> findByUrl(String url);
    boolean existByUrl(String url);

}