package club.ttg.dnd5.domain.glossary.repository;

import club.ttg.dnd5.domain.glossary.model.Glossary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GlossaryFilterRepository extends JpaRepository<Glossary, Long> {

    @Query("SELECT DISTINCT g.tagCategory FROM Glossary g WHERE g.tagCategory IS NOT NULL")
    List<String> findDistinctTagCategories();
}
