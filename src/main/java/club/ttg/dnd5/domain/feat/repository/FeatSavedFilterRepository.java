package club.ttg.dnd5.domain.feat.repository;

import club.ttg.dnd5.domain.feat.model.filter.FeatSavedFilter;
import club.ttg.dnd5.domain.filter.repository.SavedFilterRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatSavedFilterRepository extends SavedFilterRepository<FeatSavedFilter> {
}
