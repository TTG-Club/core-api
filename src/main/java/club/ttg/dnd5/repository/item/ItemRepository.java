package club.ttg.dnd5.repository.item;

import club.ttg.dnd5.model.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, String>,
        JpaSpecificationExecutor<Item>  {
}
