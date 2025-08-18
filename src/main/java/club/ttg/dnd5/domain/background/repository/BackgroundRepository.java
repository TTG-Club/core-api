package club.ttg.dnd5.domain.background.repository;

import club.ttg.dnd5.domain.background.model.Background;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackgroundRepository extends JpaRepository<Background, String>,
        JpaSpecificationExecutor<Background> {
    @Query(value = """
            select b from Background b
            where b.name ilike concat('%', :searchLine, '%')
               or b.english ilike concat('%', :searchLine, '%')
               or b.alternative ilike concat('%', :searchLine, '%')
               or b.name ilike concat('%', :invertedSearchLine, '%')
               or b.english ilike concat('%', :invertedSearchLine, '%')
               or b.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    List<Background> findBySearchLine(String searchLine, String invertedSearchLine, Sort sort);

    Integer countByUsername(String username);
}
