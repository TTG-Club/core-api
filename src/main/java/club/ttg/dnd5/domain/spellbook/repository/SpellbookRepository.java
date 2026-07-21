package club.ttg.dnd5.domain.spellbook.repository;

import club.ttg.dnd5.domain.spellbook.model.Spellbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpellbookRepository extends JpaRepository<Spellbook, UUID> {

    long countByOwnerUsername(String ownerUsername);

    List<Spellbook> findAllByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);

    Optional<Spellbook> findByIdAndOwnerUsername(UUID id, String ownerUsername);

    Optional<Spellbook> findByShareKey(UUID shareKey);

    /**
     * Отметка изменения книги: правка состава заклинаний не меняет строку книги,
     * поэтому updated_at обновляется явно — список книг сортируется и показывается по нему.
     */
    @Modifying
    @Query("UPDATE Spellbook b SET b.updatedAt = :now WHERE b.id = :id")
    void touch(@Param("id") UUID id, @Param("now") Instant now);
}
