package club.ttg.dnd5.domain.item.repository;

import club.ttg.dnd5.domain.item.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, String>,
        JpaSpecificationExecutor<Item> {
}
