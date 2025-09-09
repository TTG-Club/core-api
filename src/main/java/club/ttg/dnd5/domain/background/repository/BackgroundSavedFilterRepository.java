package club.ttg.dnd5.domain.background.repository;

import club.ttg.dnd5.domain.background.model.filter.BackgroundSavedFilter;
import club.ttg.dnd5.domain.filter.repository.SavedFilterRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackgroundSavedFilterRepository extends SavedFilterRepository<BackgroundSavedFilter> {
}
