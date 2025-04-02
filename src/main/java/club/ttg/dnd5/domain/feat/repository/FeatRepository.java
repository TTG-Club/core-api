package club.ttg.dnd5.domain.feat.repository;

import club.ttg.dnd5.domain.feat.model.Feat;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface FeatRepository extends JpaRepository<Feat, String> {
    @Query(value = """
            select f from Feat f
            where f.name ilike concat('%', :searchLine, '%')
               or f.english ilike concat('%', :searchLine, '%')
               or f.alternative ilike concat('%', :searchLine, '%')
               or f.name ilike concat('%', :invertedSearchLine, '%')
               or f.english ilike concat('%', :invertedSearchLine, '%')
               or f.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    Collection<Feat> findBySearchLine(String searchLine, String invertedSearchLine, Sort defaultSort);
}
