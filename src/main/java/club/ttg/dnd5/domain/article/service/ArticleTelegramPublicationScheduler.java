package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.config.properties.TelegramProperties;
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
 * Периодически отправляет в Telegram-канал новые опубликованные записи нужного типа.
 * <p>
 * Один поллер закрывает оба сценария публикации: «опубликовать сейчас» и «наступила
 * запланированная дата» — потому что видимость записи на сайте пассивна (сравнение
 * {@code publishDateTime < now} в запросе), отдельного события перехода нет.
 * <p>
 * Устойчивость и идемпотентность (claim-before-send): запись атомарно «занимаем» ДО
 * отправки ({@link ArticleService#claimTelegramPost}), а сам HTTP-вызов держим ВНЕ транзакции.
 * Занявший запись один — параллельный тик/второй инстанс её уже не отправит; сбой после
 * успешной отправки не даёт дубля (отметка уже стоит). При временной ошибке отправки отметку
 * снимаем ({@link ArticleService#releaseTelegramPost}) и повторяем на следующем тике. Признак
 * «пора и ещё не отправлено» — поле {@code telegramPostedAt} (см. {@link ArticleRepository#findDueForTelegram}).
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ArticleTelegramPublicationScheduler {

    private final ArticleRepository articleRepository;
    private final ArticleService articleService;
    private final TelegramPublisher telegramPublisher;
    private final TelegramProperties properties;

    @Scheduled(fixedDelayString = "${telegram.poll-interval:PT1M}")
    public void publishDueToTelegram() {
        if (!StringUtils.hasText(properties.getBotToken()) || !StringUtils.hasText(properties.getChatId())) {
            // Токен/канал не заданы для этого окружения — интеграция выключена (напр. локально). Тихо выходим.
            return;
        }

        Limit batch = Limit.of(Math.max(1, properties.getBatchSize()));
        postNew(batch);
        syncEdited(batch);
        deleteRemoved(batch);
    }

    /** Отправляет в канал новые записи, у которых наступила публикация и стоит галочка. */
    private void postNew(Limit batch) {
        List<Article> due = articleRepository.findDueForTelegram(Instant.now(), batch);
        int posted = 0;
        for (Article snapshot : due) {
            UUID id = snapshot.getId();
            // Занимаем запись ДО отправки: если её уже занял другой тик/инстанс — пропускаем (без дубля).
            if (!articleService.claimTelegramPost(id, Instant.now())) {
                continue;
            }
            // Перечитываем СВЕЖУЮ версию после claim: правка, прилетевшая между выборкой батча и claim,
            // попадёт в пост; правка ПОСЛЕ claim пометит запись dirty и синхронизируется отдельным проходом.
            Article article = articleRepository.findById(id).orElse(null);
            if (article == null || article.isDeleted()) {
                articleService.releaseTelegramPost(id);
                continue;
            }
            TelegramPublisher.PublishResult result;
            try {
                result = telegramPublisher.publish(article);
            } catch (RuntimeException ex) {
                // Любой сбой отправки (в т.ч. временный сбой чтения обложки из S3) не должен оставить
                // запись «занятой» навсегда — освобождаем и повторим на следующем тике.
                log.warn("Ошибка отправки {} в Telegram — освобождаю для повтора", article.getUrl(), ex);
                articleService.releaseTelegramPost(id);
                continue;
            }
            switch (result.status()) {
                case POSTED -> {
                    articleService.markTelegramSent(id, result.messageId(), result.photo());
                    posted++;
                }
                // Временный сбой — снимаем отметку, повторим на следующем тике.
                case RETRY -> articleService.releaseTelegramPost(id);
                // Перманентный отказ Telegram (битый запрос/чат) — оставляем занятой, чтобы не долбить канал бесконечно.
                case GIVE_UP -> log.warn("Telegram навсегда отклонил публикацию {} — больше не пытаюсь", article.getUrl());
            }
        }
        if (posted > 0) {
            log.info("Отправлено в Telegram записей: {} из {} готовых", posted, due.size());
        }
    }

    /** Синхронизирует пост в канале для записей, изменённых после отправки. */
    private void syncEdited(Limit batch) {
        List<Article> dirty = articleRepository.findDirtyForTelegram(Instant.now(), batch);
        int synced = 0;
        for (Article article : dirty) {
            TelegramPublisher.EditResult result;
            try {
                result = telegramPublisher.editPost(article);
            } catch (RuntimeException ex) {
                log.warn("Ошибка синхронизации поста {} — повторю на следующем тике", article.getUrl(), ex);
                continue;
            }
            if (result != TelegramPublisher.EditResult.RETRY) {
                // SYNCED или GIVE_UP — снимаем флаг, но только если запись не изменилась в окне отправки
                // (иначе свежая правка сдвинула updatedAt, флаг останется и синхронизируется следующим тиком).
                articleService.clearTelegramDirty(article.getId(), article.getUpdatedAt());
            }
            if (result == TelegramPublisher.EditResult.SYNCED) {
                synced++;
            }
        }
        if (synced > 0) {
            log.info("Синхронизировано правок постов в Telegram: {} из {}", synced, dirty.size());
        }
    }

    /** Удаляет из канала посты новостей, удалённых с сайта. */
    private void deleteRemoved(Limit batch) {
        List<Article> removed = articleRepository.findDeletedForTelegram(batch);
        int deleted = 0;
        for (Article article : removed) {
            TelegramPublisher.EditResult result;
            try {
                result = telegramPublisher.deletePost(article);
            } catch (RuntimeException ex) {
                log.warn("Ошибка удаления поста {} из Telegram — повторю на следующем тике", article.getUrl(), ex);
                continue;
            }
            if (result != TelegramPublisher.EditResult.RETRY) {
                // SYNCED (удалили) или GIVE_UP (поста уже нет / нельзя удалить) — сбрасываем маркер, не долбим.
                articleService.clearTelegramPost(article.getId());
            }
            if (result == TelegramPublisher.EditResult.SYNCED) {
                deleted++;
            }
        }
        if (deleted > 0) {
            log.info("Удалено постов из Telegram: {}", deleted);
        }
    }
}
