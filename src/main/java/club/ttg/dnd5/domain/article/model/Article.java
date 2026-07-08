package club.ttg.dnd5.domain.article.model;

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
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "article",
        indexes = {
                @Index(name = "article_url_index", columnList = "url", unique = true)
        })
public class Article extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String url;

    private String title;

    private String previewImageUrl;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Instant publishDateTime;

    @Column(columnDefinition = "TEXT")
    private String preview;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArticleType type;

    @Column(nullable = false)
    private boolean deleted;

    /**
     * Черновик: никогда не публиковалась. Не видна на сайте, по ссылке недоступна.
     */
    @Column(nullable = false)
    private boolean draft;

    /**
     * Флаг публикации (активности): для опубликованной записи — включена ли она
     * в общий доступ. false = снята с публикации (неактивна).
     */
    @Column(nullable = false)
    private boolean active;

    /**
     * Доступность по прямой ссылке: если true, запись можно открыть по url,
     * даже когда она не в общем доступе (актуально для неактивной записи).
     */
    @Column(nullable = false)
    private boolean accessibleByLink;

    /**
     * Вычисляемый статус: черновик / запланирована / активна / неактивна.
     */
    @Transient
    public ArticleStatus getStatus() {
        if (draft) {
            return ArticleStatus.DRAFT;
        }
        if (!active) {
            return ArticleStatus.INACTIVE;
        }
        if (publishDateTime != null && publishDateTime.isAfter(Instant.now())) {
            return ArticleStatus.SCHEDULED;
        }
        return ArticleStatus.ACTIVE;
    }
}
