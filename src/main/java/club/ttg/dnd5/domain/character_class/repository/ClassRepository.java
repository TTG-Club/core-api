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

    @Query(value = """
        select distinct c.srd_version
        from class c
        where c.srd_version is not null
        order by c.srd_version
        """, nativeQuery = true)
    List<String> findDistinctSrdVersions();
}
