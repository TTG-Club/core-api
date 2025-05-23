package club.ttg.dnd5.domain.beastiary.repository;

import club.ttg.dnd5.domain.beastiary.model.section.CreatureSection;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreatureSectionRepository extends JpaRepository<CreatureSection, String> {
    @Query(value = """
            select b from CreatureSection b
            where b.name ilike concat('%', :searchLine, '%')
               or b.english ilike concat('%', :searchLine, '%')
               or b.alternative ilike concat('%', :searchLine, '%')
               or b.name ilike concat('%', :invertedSearchLine, '%')
               or b.english ilike concat('%', :invertedSearchLine, '%')
               or b.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    List<CreatureSection> findBySearchLine(String searchLine, String invertedSearchLine, Sort sort);
}
