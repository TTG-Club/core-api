package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.config.properties.DiscordProperties;
import club.ttg.dnd5.domain.article.model.Article;
import club.ttg.dnd5.domain.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Периодически отправляет в Discord-канал новые опубликованные записи нужного типа — через входящий вебхук.
 * <p>
 * Полный аналог {@link ArticleTelegramPublicationScheduler}, но независимый: своя галочка
 * ({@code publishToDiscord}), свои отметки ({@code discord*}) и свой канал (вебхук). Один поллер закрывает
 * оба сценария публикации: «опубликовать сейчас» и «наступила запланированная дата» — потому что видимость
 * записи на сайте пассивна (сравнение {@code publishDateTime < now} в запросе), отдельного события нет.
 * <p>
 * Устойчивость и идемпотентность (claim-before-send): запись атомарно «занимаем» ДО отправки
 * ({@link ArticleService#claimDiscordPost}), а сам HTTP-вызов держим ВНЕ транзакции. Занявший запись один —
 * параллельный тик/второй инстанс её уже не отправит; сбой после успешной отправки не даёт дубля. При
 * временной ошибке отправки отметку снимаем ({@link ArticleService#releaseDiscordPost}) и повторяем на
 * следующем тике.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ArticleDiscordPublicationScheduler {

    private final ArticleRepository articleRepository;
    private final ArticleService articleService;
    private final DiscordPublisher discordPublisher;
    private final DiscordProperties properties;

    @Scheduled(fixedDelayString = "${discord.poll-interval:PT1M}")
    public void publishDueToDiscord() {
        if (!StringUtils.hasText(properties.getWebhookUrl())) {
            // Вебхук не задан для этого окружения — интеграция выключена (напр. локально). Тихо выходим.
            return;
        }

        Limit batch = Limit.of(Math.max(1, properties.getBatchSize()));
        postNew(batch);
        syncEdited(batch);
        deleteRemoved(batch);
    }

    /** Отправляет в канал новые записи, у которых наступила публикация и стоит галочка. */
    private void postNew(Limit batch) {
        List<Article> due = articleRepository.findDueForDiscord(Instant.now(), batch);
        int posted = 0;
        for (Article snapshot : due) {
            UUID id = snapshot.getId();
            // Занимаем запись ДО отправки: если её уже занял другой тик/инстанс — пропускаем (без дубля).
            if (!articleService.claimDiscordPost(id, Instant.now())) {
                continue;
            }
            // Перечитываем СВЕЖУЮ версию после claim: правка, прилетевшая между выборкой батча и claim,
            // попадёт в пост; правка ПОСЛЕ claim пометит запись dirty и синхронизируется отдельным проходом.
            Article article = articleRepository.findById(id).orElse(null);
            if (article == null || article.isDeleted()) {
                articleService.releaseDiscordPost(id);
                continue;
            }
            DiscordPublisher.PublishResult result;
            try {
                result = discordPublisher.publish(article);
            } catch (RuntimeException ex) {
                // Любой сбой отправки (в т.ч. временный сбой чтения обложки из S3) не должен оставить
                // запись «занятой» навсегда — освобождаем и повторим на следующем тике.
                log.warn("Ошибка отправки {} в Discord — освобождаю для повтора", article.getUrl(), ex);
                articleService.releaseDiscordPost(id);
                continue;
            }
            switch (result.status()) {
                case POSTED -> {
                    articleService.markDiscordSent(id, result.messageId());
                    posted++;
                }
                // Временный сбой — снимаем отметку, повторим на следующем тике.
                case RETRY -> articleService.releaseDiscordPost(id);
                // Перманентный отказ Discord (битый запрос/вебхук) — оставляем занятой, чтобы не долбить канал.
                case GIVE_UP -> log.warn("Discord навсегда отклонил публикацию {} — больше не пытаюсь", article.getUrl());
            }
        }
        if (posted > 0) {
            log.info("Отправлено в Discord записей: {} из {} готовых", posted, due.size());
        }
    }

    /** Синхронизирует пост в канале для записей, изменённых после отправки. */
    private void syncEdited(Limit batch) {
        List<Article> dirty = articleRepository.findDirtyForDiscord(Instant.now(), batch);
        int synced = 0;
        for (Article article : dirty) {
            DiscordPublisher.EditResult result;
            try {
                result = discordPublisher.editPost(article);
            } catch (RuntimeException ex) {
                log.warn("Ошибка синхронизации поста {} в Discord — повторю на следующем тике", article.getUrl(), ex);
                continue;
            }
            if (result != DiscordPublisher.EditResult.RETRY) {
                // SYNCED или GIVE_UP — снимаем флаг, но только если запись не изменилась в окне отправки
                // (иначе свежая правка сдвинула updatedAt, флаг останется и синхронизируется следующим тиком).
                articleService.clearDiscordDirty(article.getId(), article.getUpdatedAt());
            }
            if (result == DiscordPublisher.EditResult.SYNCED) {
                synced++;
            }
        }
        if (synced > 0) {
            log.info("Синхронизировано правок постов в Discord: {} из {}", synced, dirty.size());
        }
    }

    /** Удаляет из канала посты новостей, удалённых с сайта. */
    private void deleteRemoved(Limit batch) {
        List<Article> removed = articleRepository.findDeletedForDiscord(batch);
        int deleted = 0;
        for (Article article : removed) {
            DiscordPublisher.EditResult result;
            try {
                result = discordPublisher.deletePost(article);
            } catch (RuntimeException ex) {
                log.warn("Ошибка удаления поста {} из Discord — повторю на следующем тике", article.getUrl(), ex);
                continue;
            }
            if (result != DiscordPublisher.EditResult.RETRY) {
                // SYNCED (удалили) или GIVE_UP (поста уже нет / нельзя удалить) — сбрасываем маркер, не долбим.
                articleService.clearDiscordPost(article.getId());
            }
            if (result == DiscordPublisher.EditResult.SYNCED) {
                deleted++;
            }
        }
        if (deleted > 0) {
            log.info("Удалено постов из Discord: {}", deleted);
        }
    }
}
