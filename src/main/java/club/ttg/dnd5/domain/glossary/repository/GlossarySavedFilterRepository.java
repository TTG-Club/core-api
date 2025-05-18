package club.ttg.dnd5.domain.glossary.repository;

import club.ttg.dnd5.domain.filter.repository.SavedFilterRepository;
import club.ttg.dnd5.domain.glossary.model.filter.GlossarySavedFilter;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GlossarySavedFilterRepository extends SavedFilterRepository<GlossarySavedFilter> {
    @Query("SELECT DISTINCT g.tagCategory FROM Glossary g WHERE g.tagCategory IS NOT NULL")
    List<String> findDistinctTagCategories();
}
