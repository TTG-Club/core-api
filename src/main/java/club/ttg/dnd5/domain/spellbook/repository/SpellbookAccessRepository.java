package club.ttg.dnd5.domain.spellbook.repository;

import club.ttg.dnd5.domain.spellbook.model.SpellbookAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpellbookAccessRepository extends JpaRepository<SpellbookAccess, UUID> {

    /** Доступы пользователя, последние выданные первее — порядок списка доступных книг. */
    List<SpellbookAccess> findAllByUserUsernameOrderByCreatedAtDesc(String userUsername);

    Optional<SpellbookAccess> findBySpellbookIdAndUserUsername(UUID spellbookId, String userUsername);

    boolean existsBySpellbookIdAndUserUsername(UUID spellbookId, String userUsername);

    /** Убирает книгу из отображения одного пользователя; у остальных доступ сохраняется. */
    @Modifying
    @Query("DELETE FROM SpellbookAccess a WHERE a.spellbookId = :spellbookId AND a.userUsername = :username")
    void deleteBySpellbookIdAndUserUsername(@Param("spellbookId") UUID spellbookId,
                                            @Param("username") String username);

    /** Снимает доступ у всех — при удалении самой книги. */
    @Modifying
    @Query("DELETE FROM SpellbookAccess a WHERE a.spellbookId = :spellbookId")
    void deleteAllBySpellbookId(@Param("spellbookId") UUID spellbookId);
}
