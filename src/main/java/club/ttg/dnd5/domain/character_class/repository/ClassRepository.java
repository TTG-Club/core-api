package club.ttg.dnd5.domain.character_class.repository;

import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClassRepository extends JpaRepository<CharacterClass, String> {

    @Query("""
                    select cc from CharacterClass cc
                    join fetch cc.subclasses
                     where cc.parent is null
            """)
    List<CharacterClass> findAllClassesFetchSubclasses();

    Collection<CharacterClass> findAllByParentIsNull(final Sort by);

    @Query(value = """
        select c from CharacterClass c
        where c.name ilike concat('%', :searchLine, '%')
            or c.english ilike concat('%', :searchLine, '%')
            or c.alternative ilike concat('%', :searchLine, '%')
            or c.name ilike concat('%', :invertedSearchLine, '%')
            or c.english ilike concat('%', :invertedSearchLine, '%')
            or c.alternative ilike concat('%', :invertedSearchLine, '%')
    """)
    List<CharacterClass> findAllSearch(String searchLine, String invertedSearchLine, Sort sort);

    Optional<CharacterClass> findByUrl(String url);
}
