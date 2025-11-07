package club.ttg.dnd5.domain.character_class.repository;

import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClassRepository extends JpaRepository<CharacterClass, String> {

    Collection<CharacterClass> findAllByParentIsNull(final Sort by);

    @Query("""
    SELECT c FROM CharacterClass c
    WHERE c.parent IS NOT NULL
    ORDER BY
      CASE c.source.type
        WHEN 'OFFICIAL' then 1
        WHEN 'SETTING' then 2
        WHEN 'MODULE' then 3
        WHEN 'TEST' then 4
        WHEN 'THIRD_PARTY' then 5
        WHEN 'CUSTOM' then 6
      end,
      c.parent.name,
      c.name
    """)
    Collection<CharacterClass> findAllByParentIsNotNull();

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

    List<CharacterClass> findAllByParentIsNullAndCasterTypeNot(CasterType casterType);

    List<CharacterClass> findAllByParentIsNotNullAndCasterTypeNot(CasterType casterType);

}
