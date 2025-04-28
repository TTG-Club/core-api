package club.ttg.dnd5.domain.item.repository;

import club.ttg.dnd5.domain.item.model.Item;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ItemRepository extends JpaRepository<Item, String>,
        JpaSpecificationExecutor<Item> {
    @Query(value = """
            select i from Item i
            where i.name ilike concat('%', :searchLine, '%')
               or i.english ilike concat('%', :searchLine, '%')
               or i.alternative ilike concat('%', :searchLine, '%')
               or i.name ilike concat('%', :invertedSearchLine, '%')
               or i.english ilike concat('%', :invertedSearchLine, '%')
               or i.alternative ilike concat('%', :invertedSearchLine, '%')
            """
    )
    Collection<Item> findBySearchLine(String searchLine, String invertedSearchLine, Sort sort);
}
