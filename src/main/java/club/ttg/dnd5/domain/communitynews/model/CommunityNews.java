package club.ttg.dnd5.domain.communitynews.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Новость сообщества: короткая заметка со ссылкой на источник и, при желании, роликом.
 * Отдельный от статей инструмент со своими правами — без тегов, рейтинга, ревизий и кросс-постинга.
 * Публикация устроена как у статей: черновик / активна / отложенная дата / доступ по ссылке / мягкое удаление.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "community_news",
        indexes = {
                @Index(name = "community_news_publication_index",
                        columnList = "deleted, draft, active, publish_date_time")
        })
public class CommunityNews extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private String title;

    /** Автор заметки (кто прислал / чья новость). NULL — не указан. */
    @Column(length = 128)
    private String authorName;

    /** Ссылка на первоисточник новости. NULL — нет. */
    @Column(length = 512)
    private String sourceUrl;

    private String previewImageUrl;

    /** Ссылка на ролик YouTube или ВК Видео: на сайте плеер вместо обложки. NULL — видео нет. */
    @Column(length = 512)
    private String videoUrl;

    @Column(columnDefinition = "TEXT")
    private String preview;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Instant publishDateTime;

    /** Черновик: никогда не публиковалась. Не видна на сайте, по ссылке недоступна. */
    @Column(nullable = false)
    private boolean draft;

    /** Для опубликованной записи — в общем доступе ли она. false — снята с публикации. */
    @Column(nullable = false)
    private boolean active;

    /** Неактивную запись можно открыть по прямой ссылке. */
    @Column(nullable = false)
    private boolean accessibleByLink;

    @Column(nullable = false)
    private boolean deleted;

    /** Вычисляемый статус: черновик / запланирована / активна / неактивна. */
    @Transient
    public CommunityNewsStatus getStatus() {
        if (draft) {
            return CommunityNewsStatus.DRAFT;
        }
        if (!active) {
            return CommunityNewsStatus.INACTIVE;
        }
        if (publishDateTime != null && publishDateTime.isAfter(Instant.now())) {
            return CommunityNewsStatus.SCHEDULED;
        }
        return CommunityNewsStatus.ACTIVE;
    }
}
