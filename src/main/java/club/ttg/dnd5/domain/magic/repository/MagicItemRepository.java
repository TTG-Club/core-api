package club.ttg.dnd5.domain.magic.repository;

import club.ttg.dnd5.domain.magic.model.MagicItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface MagicItemRepository extends JpaRepository<MagicItem, String> {
    @Query(value = """
            select mi from MagicItem mi
            where mi.name ilike concat('%', :searchLine, '%')
               or mi.english ilike concat('%', :searchLine, '%')
               or mi.alternative ilike concat('%', :searchLine, '%')
               or mi.name ilike concat('%', :invertedSearchLine, '%')
               or mi.english ilike concat('%', :invertedSearchLine, '%')
               or mi.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    Collection<MagicItem> findBySearchLine(String searchLine, String invertedSearchLine);

    @Query(value = """
        select distinct mi.source
        from magic_item mi
        where mi.source is not null
        order by mi.source
        """, nativeQuery = true)
    List<String> findAllUsedSourceCodes();

    @Query(value = """
        select distinct mi.srd_version
        from magic_item mi
        where mi.srd_version is not null
        order by mi.srd_version
        """, nativeQuery = true)
    List<String> findDistinctSrdVersions();
}
