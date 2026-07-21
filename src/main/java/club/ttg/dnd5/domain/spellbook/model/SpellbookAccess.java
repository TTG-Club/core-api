package club.ttg.dnd5.domain.spellbook.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Доступ пользователя к чужой книге, полученный по ссылке: книга показывается ему в списке
 * доступных. Удаление строки убирает книгу из его отображения, саму книгу не трогает.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "spellbook_access",
        indexes = {
                @Index(name = "spellbook_access_user_index", columnList = "user_username")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "spellbook_access_unique", columnNames = {"spellbook_id", "user_username"})
        })
public class SpellbookAccess extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "spellbook_id", nullable = false)
    private UUID spellbookId;

    /** Логин пользователя, которому доступна книга. */
    @Column(name = "user_username", nullable = false)
    private String userUsername;
}
