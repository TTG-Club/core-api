package club.ttg.dnd5.domain.magic.repository;

import club.ttg.dnd5.domain.magic.model.MagicItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MagicItemRepository extends JpaRepository<MagicItem, String> {
}
