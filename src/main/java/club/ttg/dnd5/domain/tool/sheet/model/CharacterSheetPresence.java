package club.ttg.dnd5.domain.tool.sheet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Отметка «лист сейчас открыт у пользователя» — для мягкой блокировки: остальные видят, кто ещё
 * правит лист, и не перетирают правки друг друга. Сохранение отметка не запрещает — от затирания
 * защищает версия листа, а здесь только предупреждение.
 * <p>
 * Пишется в отдельную таблицу, а не в сам лист: частые отметки не должны поднимать его версию и
 * ловить конфликт с автосохранением. Записи без свежей отметки считаются ушедшими и чистятся при
 * следующих отметках того же листа.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "character_sheet_presence",
        indexes = @Index(name = "character_sheet_presence_sheet_user_index",
                columnList = "sheet_id, user_id", unique = true))
public class CharacterSheetPresence {

    @Id
    private UUID id;

    @Column(name = "sheet_id", nullable = false)
    private UUID sheetId;

    /**
     * Кто держит лист открытым — uuid пользователя из JWT (subject). Уникален в паре с листом:
     * несколько вкладок одного пользователя — одна отметка.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Время последней отметки. Свежее {@code CharacterSheetPresenceService.PRESENCE_TTL} — лист открыт.
     */
    @Column(name = "seen_at", nullable = false)
    private Instant seenAt;
}
