package club.ttg.dnd5.domain.article.repository;

import club.ttg.dnd5.domain.article.model.Article;
import club.ttg.dnd5.domain.article.model.ArticleType;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {

    boolean existsByUrl(String url);

    Optional<Article> findByUrl(String url);

    /**
     * Публичная выдача одной записи по url: доступна, если запись не удалена,
     * не черновик И (уже в общем доступе — активна и дата наступила — ЛИБО открыта
     * по прямой ссылке).
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.url = :url
              AND a.deleted = false
              AND a.draft = false
              AND (
                (a.active = true AND a.publishDateTime < :now)
                OR a.accessibleByLink = true
              )
            """)
    Optional<Article> findAccessibleByUrl(@Param("url") String url, @Param("now") Instant now);

    /**
     * Опубликованные и активные записи (публичная лента). Опциональный фильтр по типу —
     * инлайном `(:type is null or ...)`, как в остальных репозиториях проекта.
     * Параметр pattern — готовый LIKE-шаблон в нижнем регистре ("%текст%" или "%" без фильтра);
     * он собирается в сервисе, чтобы не давать Hibernate вывести тип строкового параметра как bytea.
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.deleted = false
              AND a.draft = false
              AND a.active = true
              AND a.publishDateTime < :now
              AND (:type IS NULL OR a.type = :type)
              AND LOWER(a.title) LIKE :pattern
            ORDER BY a.publishDateTime DESC
            """)
    List<Article> findPublished(@Param("now") Instant now, @Param("type") ArticleType type,
                                @Param("pattern") String pattern, Limit limit);

    /**
     * Не-живые записи (черновики + запланированные + неактивные) для модерации.
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.deleted = false
              AND (
                a.draft = true
                OR a.active = false
                OR a.publishDateTime IS NULL
                OR a.publishDateTime >= :now
              )
              AND (:type IS NULL OR a.type = :type)
              AND LOWER(a.title) LIKE :pattern
            ORDER BY a.createdAt DESC
            """)
    List<Article> findUnpublished(@Param("now") Instant now, @Param("type") ArticleType type,
                                  @Param("pattern") String pattern, Limit limit);

    /**
     * Записи, которые пора отправить в Telegram: с включённой галочкой (publishToTelegram=true),
     * живые и опубликованные (не удалены, не черновик, активны, дата публикации наступила) и ещё
     * не отправленные (telegramPostedAt IS NULL).
     * Один и тот же запрос закрывает и «опубликовано сейчас», и «наступила запланированная дата».
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.deleted = false
              AND a.draft = false
              AND a.active = true
              AND a.publishToTelegram = true
              AND a.publishDateTime < :now
              AND a.telegramPostedAt IS NULL
            ORDER BY a.publishDateTime ASC
            """)
    List<Article> findDueForTelegram(@Param("now") Instant now, Limit limit);

    /**
     * Атомарно «занимает» запись под отправку в Telegram: проставляет время ТОЛЬКО если она ещё
     * не занята ({@code telegramPostedAt IS NULL}). Занимаем ДО сетевого вызова, поэтому:
     * — параллельный тик / второй инстанс не отправят дубль (займёт кто-то один);
     * — сбой после успешной отправки (упавшая транзакция/рестарт) не приведёт к повторному посту.
     *
     * @return 1 — заняли (можно отправлять), 0 — уже занята кем-то (пропускаем).
     */
    @Modifying
    @Query("UPDATE Article a SET a.telegramPostedAt = :postedAt WHERE a.id = :id AND a.telegramPostedAt IS NULL")
    int claimForTelegram(@Param("id") UUID id, @Param("postedAt") Instant postedAt);

    /**
     * Снимает отметку об отправке — если отправка не удалась по временной причине, чтобы
     * повторить попытку на следующем тике.
     */
    @Modifying
    @Query("UPDATE Article a SET a.telegramPostedAt = null WHERE a.id = :id")
    void releaseTelegramClaim(@Param("id") UUID id);

    /**
     * Записи, у которых пост в канале уже есть (telegramMessageId), новость изменена после отправки
     * (telegramDirty=true) и она всё ещё публична (те же условия, что в findDueForTelegram: не удалена,
     * не черновик, активна, дата наступила) — их нужно синхронизировать (отредактировать пост).
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.deleted = false
              AND a.draft = false
              AND a.active = true
              AND a.publishToTelegram = true
              AND a.publishDateTime < :now
              AND a.telegramMessageId IS NOT NULL
              AND a.telegramDirty = true
            ORDER BY a.updatedAt ASC
            """)
    List<Article> findDirtyForTelegram(@Param("now") Instant now, Limit limit);

    /**
     * Фиксирует успешную отправку: id поста в канале и тип (фото/текст).
     * Момент отправки (telegramPostedAt) уже проставлен на этапе claim. Флаг telegramDirty НЕ трогаем:
     * если правка прилетела в окне отправки, она останется помеченной и синхронизируется следующим тиком.
     */
    @Modifying
    @Query("UPDATE Article a SET a.telegramMessageId = :messageId, a.telegramPhoto = :photo WHERE a.id = :id")
    void markTelegramSent(@Param("id") UUID id, @Param("messageId") Long messageId, @Param("photo") boolean photo);

    /**
     * Снимает флаг правки — только если запись не изменилась с момента загрузки (updatedAt совпадает).
     * Compare-and-clear: правка, прилетевшая во время отправки в канал, сдвинет updatedAt, clear не сработает,
     * и синхронизация повторится на следующем тике с самым свежим текстом.
     *
     * @return 1 — флаг снят, 0 — запись за это время изменилась (флаг оставлен).
     */
    @Modifying
    @Query("UPDATE Article a SET a.telegramDirty = false WHERE a.id = :id AND a.updatedAt = :updatedAt")
    int clearTelegramDirtyIfUnchanged(@Param("id") UUID id, @Param("updatedAt") Instant updatedAt);

    /**
     * Удалённые с сайта записи, у которых ещё есть пост в канале (telegramMessageId) — их надо удалить из Telegram.
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.deleted = true
              AND a.telegramMessageId IS NOT NULL
            ORDER BY a.updatedAt ASC
            """)
    List<Article> findDeletedForTelegram(Limit limit);

    /**
     * Сбрасывает состояние отправки в Telegram (после удаления поста из канала). Обнуляет и время отправки,
     * чтобы восстановленная (снятая с удаления) запись снова опубликовалась.
     */
    @Modifying
    @Query("UPDATE Article a SET a.telegramMessageId = null, a.telegramPostedAt = null, a.telegramDirty = false "
            + "WHERE a.id = :id")
    void clearTelegramPost(@Param("id") UUID id);

    /**
     * Записи, которые пора отправить в Discord: с включённой галочкой (publishToDiscord=true),
     * живые и опубликованные (не удалены, не черновик, активны, дата публикации наступила) и ещё
     * не отправленные (discordPostedAt IS NULL). Один и тот же запрос закрывает и «опубликовано
     * сейчас», и «наступила запланированная дата». Независим от Telegram.
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.deleted = false
              AND a.draft = false
              AND a.active = true
              AND a.publishToDiscord = true
              AND a.publishDateTime < :now
              AND a.discordPostedAt IS NULL
            ORDER BY a.publishDateTime ASC
            """)
    List<Article> findDueForDiscord(@Param("now") Instant now, Limit limit);

    /**
     * Атомарно «занимает» запись под отправку в Discord: проставляет время ТОЛЬКО если она ещё
     * не занята ({@code discordPostedAt IS NULL}). Занимаем ДО сетевого вызова, поэтому:
     * — параллельный тик / второй инстанс не отправят дубль (займёт кто-то один);
     * — сбой после успешной отправки (упавшая транзакция/рестарт) не приведёт к повторному посту.
     *
     * @return 1 — заняли (можно отправлять), 0 — уже занята кем-то (пропускаем).
     */
    @Modifying
    @Query("UPDATE Article a SET a.discordPostedAt = :postedAt WHERE a.id = :id AND a.discordPostedAt IS NULL")
    int claimForDiscord(@Param("id") UUID id, @Param("postedAt") Instant postedAt);

    /**
     * Снимает отметку об отправке — если отправка не удалась по временной причине, чтобы
     * повторить попытку на следующем тике.
     */
    @Modifying
    @Query("UPDATE Article a SET a.discordPostedAt = null WHERE a.id = :id")
    void releaseDiscordClaim(@Param("id") UUID id);

    /**
     * Записи, у которых пост в канале уже есть (discordMessageId), новость изменена после отправки
     * (discordDirty=true) и она всё ещё публична (те же условия, что в findDueForDiscord) — их нужно
     * синхронизировать (отредактировать пост).
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.deleted = false
              AND a.draft = false
              AND a.active = true
              AND a.publishToDiscord = true
              AND a.publishDateTime < :now
              AND a.discordMessageId IS NOT NULL
              AND a.discordDirty = true
            ORDER BY a.updatedAt ASC
            """)
    List<Article> findDirtyForDiscord(@Param("now") Instant now, Limit limit);

    /**
     * Фиксирует успешную отправку: id поста в канале. Момент отправки (discordPostedAt) уже проставлен
     * на этапе claim. Флаг discordDirty НЕ трогаем: если правка прилетела в окне отправки, она останется
     * помеченной и синхронизируется следующим тиком.
     */
    @Modifying
    @Query("UPDATE Article a SET a.discordMessageId = :messageId WHERE a.id = :id")
    void markDiscordSent(@Param("id") UUID id, @Param("messageId") String messageId);

    /**
     * Снимает флаг правки — только если запись не изменилась с момента загрузки (updatedAt совпадает).
     * Compare-and-clear: правка, прилетевшая во время отправки в канал, сдвинет updatedAt, clear не сработает,
     * и синхронизация повторится на следующем тике с самым свежим текстом.
     *
     * @return 1 — флаг снят, 0 — запись за это время изменилась (флаг оставлен).
     */
    @Modifying
    @Query("UPDATE Article a SET a.discordDirty = false WHERE a.id = :id AND a.updatedAt = :updatedAt")
    int clearDiscordDirtyIfUnchanged(@Param("id") UUID id, @Param("updatedAt") Instant updatedAt);

    /**
     * Удалённые с сайта записи, у которых ещё есть пост в канале (discordMessageId) — их надо удалить из Discord.
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.deleted = true
              AND a.discordMessageId IS NOT NULL
            ORDER BY a.updatedAt ASC
            """)
    List<Article> findDeletedForDiscord(Limit limit);

    /**
     * Сбрасывает состояние отправки в Discord (после удаления поста из канала). Обнуляет и время отправки,
     * чтобы восстановленная (снятая с удаления) запись снова опубликовалась.
     */
    @Modifying
    @Query("UPDATE Article a SET a.discordMessageId = null, a.discordPostedAt = null, a.discordDirty = false "
            + "WHERE a.id = :id")
    void clearDiscordPost(@Param("id") UUID id);

    /**
     * Записи, которые пора отправить в VK: с включённой галочкой (publishToVk=true), живые и опубликованные
     * (не удалены, не черновик, активны, дата публикации наступила) и ещё не отправленные (vkPostedAt IS NULL).
     * Один и тот же запрос закрывает и «опубликовано сейчас», и «наступила запланированная дата». Независим
     * от Telegram и Discord.
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.deleted = false
              AND a.draft = false
              AND a.active = true
              AND a.publishToVk = true
              AND a.publishDateTime < :now
              AND a.vkPostedAt IS NULL
            ORDER BY a.publishDateTime ASC
            """)
    List<Article> findDueForVk(@Param("now") Instant now, Limit limit);

    /**
     * Атомарно «занимает» запись под отправку в VK: проставляет время ТОЛЬКО если она ещё не занята
     * ({@code vkPostedAt IS NULL}). Занимаем ДО сетевого вызова, поэтому:
     * — параллельный тик / второй инстанс не отправят дубль (займёт кто-то один);
     * — сбой после успешной отправки (упавшая транзакция/рестарт) не приведёт к повторному посту.
     *
     * @return 1 — заняли (можно отправлять), 0 — уже занята кем-то (пропускаем).
     */
    @Modifying
    @Query("UPDATE Article a SET a.vkPostedAt = :postedAt WHERE a.id = :id AND a.vkPostedAt IS NULL")
    int claimForVk(@Param("id") UUID id, @Param("postedAt") Instant postedAt);

    /**
     * Снимает отметку об отправке — если отправка не удалась по временной причине, чтобы
     * повторить попытку на следующем тике.
     */
    @Modifying
    @Query("UPDATE Article a SET a.vkPostedAt = null WHERE a.id = :id")
    void releaseVkClaim(@Param("id") UUID id);

    /**
     * Записи, у которых пост на стене уже есть (vkPostId), новость изменена после отправки (vkDirty=true) и
     * она всё ещё публична (те же условия, что в findDueForVk) — их нужно синхронизировать (отредактировать пост).
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.deleted = false
              AND a.draft = false
              AND a.active = true
              AND a.publishToVk = true
              AND a.publishDateTime < :now
              AND a.vkPostId IS NOT NULL
              AND a.vkDirty = true
            ORDER BY a.updatedAt ASC
            """)
    List<Article> findDirtyForVk(@Param("now") Instant now, Limit limit);

    /**
     * Фиксирует успешную отправку: id поста на стене и строку вложения-обложки (для сохранения фото при правке).
     * Момент отправки (vkPostedAt) уже проставлен на этапе claim. Флаг vkDirty НЕ трогаем: если правка прилетела
     * в окне отправки, она останется помеченной и синхронизируется следующим тиком.
     */
    @Modifying
    @Query("UPDATE Article a SET a.vkPostId = :postId, a.vkAttachment = :attachment WHERE a.id = :id")
    void markVkSent(@Param("id") UUID id, @Param("postId") Long postId, @Param("attachment") String attachment);

    /**
     * Сохраняет строку вложения-обложки для уже опубликованного поста — обложку залили при синхронизации
     * правки (пост изначально ушёл текстом). Точечный UPDATE только по vk_attachment: не трогает updatedAt и
     * прочие vk_*-поля, чтобы не сорвать claim/compare-and-clear.
     */
    @Modifying
    @Query("UPDATE Article a SET a.vkAttachment = :attachment WHERE a.id = :id")
    void updateVkAttachment(@Param("id") UUID id, @Param("attachment") String attachment);

    /**
     * Снимает флаг правки — только если запись не изменилась с момента загрузки (updatedAt совпадает).
     * Compare-and-clear: правка, прилетевшая во время отправки на стену, сдвинет updatedAt, clear не сработает,
     * и синхронизация повторится на следующем тике с самым свежим текстом.
     *
     * @return 1 — флаг снят, 0 — запись за это время изменилась (флаг оставлен).
     */
    @Modifying
    @Query("UPDATE Article a SET a.vkDirty = false WHERE a.id = :id AND a.updatedAt = :updatedAt")
    int clearVkDirtyIfUnchanged(@Param("id") UUID id, @Param("updatedAt") Instant updatedAt);

    /**
     * Удалённые с сайта записи, у которых ещё есть пост на стене (vkPostId) — их надо удалить из VK.
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.deleted = true
              AND a.vkPostId IS NOT NULL
            ORDER BY a.updatedAt ASC
            """)
    List<Article> findDeletedForVk(Limit limit);

    /**
     * Сбрасывает состояние отправки в VK (после удаления поста со стены). Обнуляет и время отправки,
     * чтобы восстановленная (снятая с удаления) запись снова опубликовалась.
     */
    @Modifying
    @Query("UPDATE Article a SET a.vkPostId = null, a.vkAttachment = null, a.vkPostedAt = null, a.vkDirty = false "
            + "WHERE a.id = :id")
    void clearVkPost(@Param("id") UUID id);
}
