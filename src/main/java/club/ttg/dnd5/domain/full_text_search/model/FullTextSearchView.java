package club.ttg.dnd5.domain.full_text_search.model;

import club.ttg.dnd5.domain.source.model.SourceType;
import club.ttg.dnd5.domain.common.model.SectionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Строка общего поиска по всем разделам. Ключ — пара {@code (url, type)}: url уникален только
 * внутри своего раздела, и у заклинания и существа он может совпадать («Боевой фамильяр»).
 * С ключом по одному url Hibernate склеивал такие строки в одну сущность, и существо
 * подменялось заклинанием.
 */
@Entity
@Immutable
@Table(name = "full_name_search_view")
@IdClass(FullTextSearchView.Key.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FullTextSearchView {
    @Id
    private String url;
    private String name;
    private String english;
    private String alternative;
    @Id
    @Enumerated(EnumType.STRING)
    private SectionType type;
    private Boolean isHiddenEntity;
    private String acronym;
    private String sourceName;
    private String sourceEnglish;
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;
    private Integer page;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** Составной ключ {@code (url, type)}. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {
        private String url;
        private SectionType type;

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key key)) {
                return false;
            }
            return Objects.equals(url, key.url) && type == key.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, type);
        }
    }
}
