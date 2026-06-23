package club.ttg.dnd5.domain.vttg.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Предрассчитанный VTTG-payload одной сущности (кэш экспорта). Ключ — пара {@code (type, url)},
 * совпадающая с типом раздела и естественным ключом сущности-источника.
 *
 * <p>Payload валиден, только если {@code srcUpdatedAt} совпадает с актуальным временем изменения
 * сущности и {@code schemaVer} — с версией логики маппера ({@link
 * club.ttg.dnd5.domain.vttg.service.VttgPayloadStore#SCHEMA_VERSION}). Иначе он пересчитывается
 * на лету и перезаписывается.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vttg_export")
@IdClass(VttgExport.Key.class)
public class VttgExport {

    @Id
    private String type;

    @Id
    private String url;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode payload;

    @Column(name = "src_updated_at", nullable = false)
    private Instant srcUpdatedAt;

    @Column(name = "schema_ver", nullable = false)
    private int schemaVer;

    /** Составной первичный ключ {@code (type, url)}. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {
        private String type;
        private String url;

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key key)) {
                return false;
            }
            return Objects.equals(type, key.type) && Objects.equals(url, key.url);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, url);
        }
    }
}
