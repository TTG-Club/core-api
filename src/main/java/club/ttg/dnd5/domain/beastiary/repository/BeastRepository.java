package club.ttg.dnd5.domain.beastiary.repository;

import club.ttg.dnd5.domain.beastiary.model.Beast;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeastRepository  extends JpaRepository<Beast, String> {
    @Query(value = """
            select b from Beast b
            where b.name ilike concat('%', :searchLine, '%')
               or b.english ilike concat('%', :searchLine, '%')
               or b.alternative ilike concat('%', :searchLine, '%')
               or b.name ilike concat('%', :invertedSearchLine, '%')
               or b.english ilike concat('%', :invertedSearchLine, '%')
               or b.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    List<Beast> findBySearchLine(String searchLine, String invertedSearchLine, Sort sort);
}
