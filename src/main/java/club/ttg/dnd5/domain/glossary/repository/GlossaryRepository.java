package club.ttg.dnd5.domain.glossary.repository;

import club.ttg.dnd5.domain.glossary.model.Glossary;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GlossaryRepository extends JpaRepository<Glossary, String> {
    @Query(value = """
            select g from Glossary g
            where g.name ilike concat('%', :searchLine, '%')
               or g.english ilike concat('%', :searchLine, '%')
               or g.alternative ilike concat('%', :searchLine, '%')
               or g.english ilike concat('%', :invertedSearchLine, '%')
               or g.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    List<Glossary> findBySearchLine(String searchLine, String invertedSearchLine, Sort sort);

    Integer countByUsername(String username);
}
