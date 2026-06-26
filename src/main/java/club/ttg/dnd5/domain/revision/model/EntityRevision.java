package club.ttg.dnd5.domain.revision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Единая таблица истории изменений для всех сущностей портала.
 * <p>
 * Хранит неизменяемый (append-only) снимок сущности в формате JSON на каждое
 * изменение. Снимок берётся в той же форме, что отдаётся редактору
 * ({@code GET /{url}/raw}), поэтому откат — это повторная отправка снимка
 * через обычный путь обновления.
 * <p>
 * Записи защищены от подделки на двух уровнях: триггер БД запрещает UPDATE/DELETE,
 * а поле {@link #hash} образует цепочку хэшей (каждая запись включает хэш
 * предыдущей), что делает скрытую правку середины истории обнаружимой.
 */
@Entity
@Table(name = "entity_revision")
@Getter
@Setter
@NoArgsConstructor
public class EntityRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Тип сущности, например "spell", "creature". */
    @Column(name = "entity_type", nullable = false, updatable = false)
    private String entityType;

    /** Идентификатор сущности (url). */
    @Column(name = "entity_id", nullable = false, updatable = false)
    private String entityId;

    /** Порядковый номер версии в рамках (entityType, entityId), начиная с 1. */
    @Column(nullable = false, updatable = false)
    private int version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private RevisionOperation operation;

    /** Полный снимок сущности в форме редактора. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false, updatable = false)
    private String snapshot;

    /** Логин пользователя, выполнившего изменение. */
    @Column(name = "changed_by", updatable = false)
    private String changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    /** Хэш предыдущей записи этой сущности (null для первой версии). */
    @Column(name = "prev_hash", updatable = false)
    private String prevHash;

    /** SHA-256 цепочки: hash(prevHash + полей этой записи). */
    @Column(nullable = false, updatable = false)
    private String hash;
}
