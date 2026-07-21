package club.ttg.dnd5.domain.spellbook.model;

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
 * Именованная книга заклинаний пользователя: личный набор заклинаний с отметками подготовленных.
 * Владелец обязателен — книгу может завести только зарегистрированный пользователь.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "spellbook",
        indexes = {
                @Index(name = "spellbook_owner_username_index", columnList = "owner_username")
        })
public class Spellbook extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    /** Логин владельца из JWT: менять книгу может только он. */
    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;

    /**
     * Секретный ключ ссылки: по нему другой пользователь открывает книгу и добавляет её себе
     * в доступные. Отдаётся только владельцу — из него фронт собирает ссылку для шаринга.
     */
    @Column(name = "share_key", nullable = false, unique = true)
    private UUID shareKey;
}
