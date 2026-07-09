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
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@Entity
// UPDATE только по реально изменённым колонкам: правка новости не должна затирать telegram_*/discord_*
// поля, которыми асинхронно владеют планировщики публикации (claim / message_id / dirty).
@DynamicUpdate
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
     * Пожелание автора отправить запись в Telegram-канал. Управляется галочкой в админке.
     * Постится только при publishToTelegram=true И включённой глобально интеграции, один раз
     * при попадании записи в общий доступ (см. {@code telegramPostedAt}).
     */
    @Column(nullable = false)
    private boolean publishToTelegram;

    /**
     * Момент отправки записи в Telegram-канал. NULL — ещё не отправлена.
     * Служит флагом идемпотентности: планировщик постит запись один раз и проставляет время,
     * поэтому повторное сохранение/редактирование или снятие-и-возврат публикации не дублируют пост.
     */
    private Instant telegramPostedAt;

    /**
     * id сообщения в Telegram-канале. Заполняется после успешной отправки, нужен для правки поста
     * при редактировании новости. NULL — пост в канал ещё не ушёл.
     */
    private Long telegramMessageId;

    /**
     * Пост в канале отправлен как фото (с обложкой). Тогда при синхронизации правим caption,
     * иначе — text (Telegram различает editMessageCaption и editMessageText).
     */
    @Column(nullable = false)
    private boolean telegramPhoto;

    /**
     * Новость изменена после отправки в канал — планировщик синхронизирует пост (editMessage) и снимет флаг.
     */
    @Column(nullable = false)
    private boolean telegramDirty;

    /**
     * Пожелание автора отправить запись в Discord-канал. Управляется галочкой в админке.
     * Постится только при publishToDiscord=true И включённой глобально интеграции (задан webhook),
     * один раз при попадании записи в общий доступ (см. {@code discordPostedAt}). Независима от Telegram.
     */
    @Column(nullable = false)
    private boolean publishToDiscord;

    /**
     * Момент отправки записи в Discord-канал. NULL — ещё не отправлена.
     * Служит флагом идемпотентности: планировщик постит запись один раз и проставляет время,
     * поэтому повторное сохранение/редактирование или снятие-и-возврат публикации не дублируют пост.
     */
    private Instant discordPostedAt;

    /**
     * id сообщения в Discord-канале (снежинка). Заполняется после успешной отправки, нужен для правки
     * и удаления поста. Строка, а не число: снежинка приходит строкой в JSON и используется как сегмент
     * пути вебхука ({@code /messages/{id}}). NULL — пост в канал ещё не ушёл.
     */
    @Column(length = 32)
    private String discordMessageId;

    /**
     * Новость изменена после отправки в канал — планировщик синхронизирует пост (editMessage) и снимет флаг.
     */
    @Column(nullable = false)
    private boolean discordDirty;

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
