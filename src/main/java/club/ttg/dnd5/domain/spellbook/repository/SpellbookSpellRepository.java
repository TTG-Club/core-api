package club.ttg.dnd5.domain.spellbook.repository;

import club.ttg.dnd5.domain.spellbook.model.SpellbookSpell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SpellbookSpellRepository extends JpaRepository<SpellbookSpell, UUID> {

    List<SpellbookSpell> findAllBySpellbookId(UUID spellbookId);

    Optional<SpellbookSpell> findBySpellbookIdAndSpellUrl(UUID spellbookId, String spellUrl);

    @Query("SELECT s.spellUrl FROM SpellbookSpell s WHERE s.spellbookId = :spellbookId")
    Set<String> findSpellUrlsBySpellbookId(@Param("spellbookId") UUID spellbookId);

    /**
     * Общее число заклинаний и число подготовленных по каждой книге — для списка книг
     * (одним запросом вместо выборки по каждой книге).
     */
    @Query("""
            SELECT s.spellbookId AS spellbookId,
                   COUNT(s) AS total,
                   SUM(CASE WHEN s.prepared = TRUE THEN 1 ELSE 0 END) AS prepared
            FROM SpellbookSpell s
            WHERE s.spellbookId IN :spellbookIds
            GROUP BY s.spellbookId
            """)
    List<SpellbookSpellCount> countsBySpellbookIds(@Param("spellbookIds") Collection<UUID> spellbookIds);

    @Modifying
    @Query("DELETE FROM SpellbookSpell s WHERE s.spellbookId = :spellbookId")
    void deleteAllBySpellbookId(@Param("spellbookId") UUID spellbookId);
}
