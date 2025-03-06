package club.ttg.dnd5.domain.spell.repository;

import club.ttg.dnd5.domain.spell.model.Spell;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpellRepository extends JpaRepository<Spell, String> {

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

}
