package club.ttg.dnd5.domain.item.repository;

import club.ttg.dnd5.domain.filter.repository.SavedFilterRepository;
import club.ttg.dnd5.domain.item.model.filter.ItemSavedFilter;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemSavedFilterRepository extends SavedFilterRepository<ItemSavedFilter> {
}
