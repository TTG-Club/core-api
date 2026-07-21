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
 * Заклинание в книге. Уровень и прочие данные не дублируются — они читаются из самого заклинания,
 * здесь хранится только принадлежность книге и отметка «подготовлено».
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "spellbook_spell",
        indexes = {
                @Index(name = "spellbook_spell_spellbook_index", columnList = "spellbook_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "spellbook_spell_unique", columnNames = {"spellbook_id", "spell_url"})
        })
public class SpellbookSpell extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "spellbook_id", nullable = false)
    private UUID spellbookId;

    /** Слаг заклинания. Ссылка на {@code spell.url} с каскадным удалением на уровне БД. */
    @Column(name = "spell_url", nullable = false)
    private String spellUrl;

    /**
     * Заклинание подготовлено на день. Отметка личная и живёт вместе с книгой:
     * добавленное заклинание по умолчанию не подготовлено.
     */
    @Column(nullable = false)
    private boolean prepared;
}
