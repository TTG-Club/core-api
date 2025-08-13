package club.ttg.dnd5.domain.workshop.repository;

import club.ttg.dnd5.domain.workshop.rest.dto.WorkshopPairDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WorkshopRepository extends JpaRepository<Object, String> {

    @Query("""
            WITH workshop_count as (SELECT 'BACKGROUND' as section_type,
                         username     as username
                  FROM Background
                  WHERE username = :username
                  UNION ALL
                  SELECT 'BESTIARY' as section_type,
                         username   as username
                  FROM Creature
                  WHERE username = :username
                  UNION ALL
                  SELECT 'FEAT'   as section_type,
                         username as username
                  FROM Feat
                  WHERE username = :username
                  UNION ALL
                  SELECT 'GLOSSARY' as section_type,
                         username   as username
                  FROM Glossary
                  WHERE username = :username
                  UNION ALL
                  SELECT 'MAGIC_ITEM' as section_type,
                         username     as username
                  FROM MagicItem
                  WHERE username = :username
                  UNION ALL
                  SELECT 'SPECIES' as section_type,
                         username  as username
                  FROM Species
                  WHERE username = :username
                  UNION ALL
                  SELECT 'ITEM'   as section_type,
                         username as username
                  FROM Item
                  WHERE username = :username
                  UNION ALL
                  SELECT 'SPELL'  as section_type,
                         username as username
                  FROM Spell
                  WHERE username = :username)
            SELECT wc.section_type as sectionType,
                   COUNT(*)       as count
            FROM workshop_count as wc
            GROUP BY wc.section_type
            """)
    List<WorkshopPairDto> findWorkshopUserSections(String username);
}
