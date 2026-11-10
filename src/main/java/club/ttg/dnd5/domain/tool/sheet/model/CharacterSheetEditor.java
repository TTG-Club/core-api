package club.ttg.dnd5.domain.tool.sheet.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Право другого пользователя редактировать лист персонажа — от запроса до решения владельца.
 * <p>
 * Выдаётся только по запросу: просить может тот, кто уже сохранил лист по ссылке «поделиться»,
 * поэтому перебирать логины или почты некому. Связи с {@link CharacterSheet} через JPA нет, как и у
 * {@link SavedCharacterSheet}: записи чистятся явно — при отзыве ссылки и вытеснении листа из истории.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "character_sheet_editor",
        indexes = {
                @Index(name = "character_sheet_editor_sheet_user_index",
                        columnList = "sheet_id, user_id", unique = true),
                @Index(name = "character_sheet_editor_user_id_index", columnList = "user_id")
        })
public class CharacterSheetEditor extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Лист, на который просят право.
     */
    @Column(name = "sheet_id", nullable = false)
    private UUID sheetId;

    /**
     * Кто просит — uuid пользователя из JWT (subject). Уникален в паре с листом: повторный запрос
     * обновляет запись, а не плодит дубли.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CharacterSheetEditorStatus status;

    /**
     * Когда владелец ответил на запрос; {@code null} — ещё не отвечал. От отказа отсчитывается пауза
     * до повторного запроса.
     */
    @Column(name = "decided_at")
    private Instant decidedAt;
}
