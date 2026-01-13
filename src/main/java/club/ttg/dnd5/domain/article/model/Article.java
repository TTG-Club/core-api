package club.ttg.dnd5.domain.article.model;

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

    private Boolean deleted;
}
