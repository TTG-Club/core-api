package club.ttg.dnd5.domain.tool.sheet.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Чужой лист персонажа, сохранённый пользователем по ссылке «поделиться». Хранится сама ссылка,
 * а не копия документа: лист остаётся у владельца и виден сохранившему только на чтение.
 * <p>
 * Связи с {@link CharacterSheet} через JPA намеренно нет: запись переживает и удаление листа,
 * и отзыв доступа — пользователь должен увидеть, что лист стал недоступен, а не его молчаливое
 * исчезновение из списка.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "character_sheet_saved",
        indexes = {
                @Index(name = "character_sheet_saved_user_id_index", columnList = "user_id"),
                @Index(name = "character_sheet_saved_user_sheet_index",
                        columnList = "user_id, sheet_id", unique = true)
        })
public class SavedCharacterSheet extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Тот, кто сохранил ссылку, — uuid пользователя из JWT (subject).
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Идентификатор сохранённого листа. Уникален в паре с владельцем записи: повторное сохранение
     * того же листа по новой ссылке обновляет токен, а не создаёт вторую запись.
     */
    @Column(name = "sheet_id", nullable = false)
    private UUID sheetId;

    /**
     * Токен ссылки на момент сохранения. Доступ считается живым, только пока он совпадает с токеном
     * листа: отзыв доступа владельцем гасит сохранённую ссылку, а новый share выдаёт новый токен —
     * и требует новой ссылки.
     */
    @Column(name = "share_token", nullable = false)
    private UUID shareToken;

    /**
     * Название листа на момент сохранения. Нужно недоступным записям: живое название такого листа
     * взять неоткуда, а карточка без подписи не объясняет, что именно пропало.
     */
    @Column(nullable = false)
    private String name;
}
