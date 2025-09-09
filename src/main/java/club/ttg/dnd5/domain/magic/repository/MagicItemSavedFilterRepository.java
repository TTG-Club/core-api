package club.ttg.dnd5.domain.magic.repository;

import club.ttg.dnd5.domain.filter.repository.SavedFilterRepository;
import club.ttg.dnd5.domain.magic.model.filter.MagicItemSavedFilter;
import org.springframework.stereotype.Repository;

@Repository
public interface MagicItemSavedFilterRepository extends SavedFilterRepository<MagicItemSavedFilter> {
}
