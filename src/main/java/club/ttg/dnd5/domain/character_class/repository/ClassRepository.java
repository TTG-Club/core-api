package club.ttg.dnd5.domain.character_class.repository;

import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClassRepository extends JpaRepository<CharacterClass, String> {

    Collection<CharacterClass> findAllByParentIsNull(final Sort by);

    @Query("""
    SELECT c FROM CharacterClass c
    WHERE c.parent IS NOT NULL
    ORDER BY
      CASE c.source.origin
        WHEN 'OFFICIAL' then 1
        WHEN 'THIRD_PARTY' then 2
        WHEN 'HOMEBREW' then 3
      end,
      CASE c.source.kind
        WHEN 'SOURCEBOOK' then 1
        WHEN 'SETTING' then 2
        WHEN 'ADVENTURE' then 3
      end,
      c.parent.name,
      c.name
    """)
    Collection<CharacterClass> findAllByParentIsNotNull();

    Optional<CharacterClass> findByUrl(String url);

    List<CharacterClass> findAllByParentIsNullAndCasterTypeNot(CasterType casterType);

    @Query("""
    SELECT distinct cc
    FROM CharacterClass cc
    WHERE cc.parent is not null
      AND cc.casterType <> :casterType
      AND EXISTS (
          SELECT 1
          FROM Spell s
          JOIN s.subclassAffiliation sub
          WHERE sub = cc
      )
    """)
    List<CharacterClass> findAllSubclassesWithSpellAffiliationAndCasterTypeNot(@Param("casterType") CasterType casterType);

    @Query(value = """
        select distinct c.source
        from class c
        where c.source is not null
        order by c.source
        """, nativeQuery = true)
    List<String> findAllUsedSourceCodes();
}
