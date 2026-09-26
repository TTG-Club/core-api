package club.ttg.dnd5.domain.vttg.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Текущая версия формата выгрузки VTTG. Таблица из одной строки ({@code id = 1}).
 *
 * <p>Версия пишется в {@code vttg_export.schema_ver} и отдаётся клиентам как {@code schemaVersion}:
 * смена версии делает все предрассчитанные payload устаревшими, а клиент VTTG, увидев новую
 * версию, перекачивает компендиум целиком. Меняется только атомарным UPDATE в репозитории.</p>
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "vttg_compendium_version")
public class VttgCompendiumVersion {

    /** Идентификатор единственной строки. */
    public static final short SINGLE_ROW_ID = 1;

    @Id
    private Short id;

    @Column(nullable = false)
    private int version;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;
}
