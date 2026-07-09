package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.config.properties.VkProperties;
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
 * Периодически публикует на стену сообщества ВКонтакте новые опубликованные записи нужного типа — через
 * ключ доступа сообщества, без бота.
 * <p>
 * Полный аналог {@link ArticleDiscordPublicationScheduler}, но независимый: своя галочка ({@code publishToVk}),
 * свои отметки ({@code vk*}) и своё сообщество. Один поллер закрывает оба сценария публикации: «опубликовать
 * сейчас» и «наступила запланированная дата» — потому что видимость записи на сайте пассивна (сравнение
 * {@code publishDateTime < now} в запросе), отдельного события нет.
 * <p>
 * Устойчивость и идемпотентность (claim-before-send): запись атомарно «занимаем» ДО отправки
 * ({@link ArticleService#claimVkPost}), а сам HTTP-вызов держим ВНЕ транзакции. Занявший запись один —
 * параллельный тик/второй инстанс её уже не отправит; сбой после успешной отправки не даёт дубля. При
 * временной ошибке отправки отметку снимаем ({@link ArticleService#releaseVkPost}) и повторяем на следующем тике.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ArticleVkPublicationScheduler {

    private final ArticleRepository articleRepository;
    private final ArticleService articleService;
    private final VkPublisher vkPublisher;
    private final VkProperties properties;

    @Scheduled(fixedDelayString = "${vk.poll-interval:PT1M}")
    public void publishDueToVk() {
        if (!StringUtils.hasText(properties.getAccessToken()) || !StringUtils.hasText(properties.getGroupId())) {
            // Токен/сообщество не заданы для этого окружения — интеграция выключена (напр. локально). Тихо выходим.
            return;
        }

        Limit batch = Limit.of(Math.max(1, properties.getBatchSize()));
        postNew(batch);
        syncEdited(batch);
        deleteRemoved(batch);
    }

    /** Публикует новые записи, у которых наступила публикация и стоит галочка. */
    private void postNew(Limit batch) {
        List<Article> due = articleRepository.findDueForVk(Instant.now(), batch);
        int posted = 0;
        for (Article snapshot : due) {
            UUID id = snapshot.getId();
            // Занимаем запись ДО отправки: если её уже занял другой тик/инстанс — пропускаем (без дубля).
            if (!articleService.claimVkPost(id, Instant.now())) {
                continue;
            }
            // Перечитываем СВЕЖУЮ версию после claim: правка, прилетевшая между выборкой батча и claim,
            // попадёт в пост; правка ПОСЛЕ claim пометит запись dirty и синхронизируется отдельным проходом.
            Article article = articleRepository.findById(id).orElse(null);
            if (article == null || article.isDeleted()) {
                articleService.releaseVkPost(id);
                continue;
            }
            VkPublisher.PublishResult result;
            try {
                result = vkPublisher.publish(article);
            } catch (RuntimeException ex) {
                // Любой сбой отправки (в т.ч. временный сбой чтения обложки из S3) не должен оставить
                // запись «занятой» навсегда — освобождаем и повторим на следующем тике.
                log.warn("Ошибка отправки {} в VK — освобождаю для повтора", article.getUrl(), ex);
                articleService.releaseVkPost(id);
                continue;
            }
            switch (result.status()) {
                case POSTED -> {
                    articleService.markVkSent(id, result.postId(), result.attachment());
                    posted++;
                }
                // Временный сбой — снимаем отметку, повторим на следующем тике.
                case RETRY -> articleService.releaseVkPost(id);
                // Перманентный отказ VK (битый запрос/токен) — оставляем занятой, чтобы не долбить стену.
                case GIVE_UP -> log.warn("VK навсегда отклонил публикацию {} — больше не пытаюсь", article.getUrl());
            }
        }
        if (posted > 0) {
            log.info("Отправлено в VK записей: {} из {} готовых", posted, due.size());
        }
    }

    /** Синхронизирует пост на стене для записей, изменённых после отправки. */
    private void syncEdited(Limit batch) {
        List<Article> dirty = articleRepository.findDirtyForVk(Instant.now(), batch);
        int synced = 0;
        for (Article article : dirty) {
            VkPublisher.EditResult result;
            try {
                result = vkPublisher.editPost(article);
            } catch (RuntimeException ex) {
                log.warn("Ошибка синхронизации поста {} в VK — повторю на следующем тике", article.getUrl(), ex);
                continue;
            }
            if (result != VkPublisher.EditResult.RETRY) {
                // SYNCED или GIVE_UP — снимаем флаг, но только если запись не изменилась в окне отправки
                // (иначе свежая правка сдвинула updatedAt, флаг останется и синхронизируется следующим тиком).
                articleService.clearVkDirty(article.getId(), article.getUpdatedAt());
            }
            if (result == VkPublisher.EditResult.SYNCED) {
                synced++;
            }
        }
        if (synced > 0) {
            log.info("Синхронизировано правок постов в VK: {} из {}", synced, dirty.size());
        }
    }

    /** Удаляет со стены посты новостей, удалённых с сайта. */
    private void deleteRemoved(Limit batch) {
        List<Article> removed = articleRepository.findDeletedForVk(batch);
        int deleted = 0;
        for (Article article : removed) {
            VkPublisher.EditResult result;
            try {
                result = vkPublisher.deletePost(article);
            } catch (RuntimeException ex) {
                log.warn("Ошибка удаления поста {} из VK — повторю на следующем тике", article.getUrl(), ex);
                continue;
            }
            if (result != VkPublisher.EditResult.RETRY) {
                // SYNCED (удалили) или GIVE_UP (поста уже нет / нельзя удалить) — сбрасываем маркер, не долбим.
                articleService.clearVkPost(article.getId());
            }
            if (result == VkPublisher.EditResult.SYNCED) {
                deleted++;
            }
        }
        if (deleted > 0) {
            log.info("Удалено постов из VK: {}", deleted);
        }
    }
}
