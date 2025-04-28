package club.ttg.dnd5.domain.magic.repository;

import club.ttg.dnd5.domain.magic.model.MagicItem;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;

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
    Collection<MagicItem> findBySearchLine(String searchLine, String invertedSearchLine, Sort sort);
}
