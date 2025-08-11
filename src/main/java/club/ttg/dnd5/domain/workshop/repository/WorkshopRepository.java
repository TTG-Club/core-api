package club.ttg.dnd5.domain.workshop.repository;

import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.workshop.rest.dto.WorkshopPairDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WorkshopRepository extends JpaRepository<Spell, String> {

    @Query(" select t.section_type as sectionType," +
            " COUNT(*) as count " +
            " from (select 'BACKGROUND' as section_type, " +
            "                     username as username " +
            "              from Background " +
            "              where username = :username " +
            "              union all " +
            "              select 'BESTIARY' as section_type, " +
            "                     username as username " +
            "              from Creature " +
            "              where username = :username " +
            "              union all " +
            "              select 'FEAT' as section_type, " +
            "                     username as username " +
            "              from Feat " +
            "              where username = :username " +
            "              union all " +
            "              select 'GLOSSARY' as section_type, " +
            "                     username as username " +
            "              from Glossary " +
            "              where username = :username " +
            "              union all " +
            "              select 'MAGIC_ITEM' as section_type, " +
            "                     username as username " +
            "              from MagicItem " +
            "              where username = :username " +
            "              union all " +
            "              select 'SPECIES' as section_type, " +
            "                     username as username " +
            "              from Species " +
            "              where username = :username " +
            "              union all " +
            "              select 'ITEM' as section_type, " +
            "                     username as username " +
            "              from Item " +
            "              where username = :username " +
            "              union all " +
            "              select 'SPELL' as section_type, " +
            "                     username as username " +
            "              from Spell " +
            "              where username = :username ) as t " +
            " group by t.section_type ")
    List<WorkshopPairDto> findWorkshopUserStatistics(String username);
}
