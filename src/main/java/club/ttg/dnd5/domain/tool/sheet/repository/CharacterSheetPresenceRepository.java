package club.ttg.dnd5.domain.tool.sheet.repository;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetPresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CharacterSheetPresenceRepository extends JpaRepository<CharacterSheetPresence, UUID> {

    /**
     * Ставит или освежает отметку одним запросом. Не «найти и сохранить»: две вкладки одного
     * пользователя шлют отметки одновременно, и вторая вставка упала бы на уникальном индексе.
     */
    @Modifying
    @Query(value = "insert into character_sheet_presence (id, sheet_id, user_id, seen_at) "
            + "values (:id, :sheetId, :userId, :seenAt) "
            + "on conflict (sheet_id, user_id) do update set seen_at = excluded.seen_at",
            nativeQuery = true)
    void upsert(@Param("id") UUID id,
                @Param("sheetId") UUID sheetId,
                @Param("userId") UUID userId,
                @Param("seenAt") Instant seenAt);

    List<CharacterSheetPresence> findAllBySheetIdAndUserIdNotAndSeenAtAfter(UUID sheetId, UUID userId,
                                                                         Instant seenAfter);

    void deleteAllBySheetIdAndSeenAtBefore(UUID sheetId, Instant seenBefore);

    void deleteBySheetIdAndUserId(UUID sheetId, UUID userId);

    void deleteAllBySheetIdIn(Collection<UUID> sheetIds);
}
