package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.domain.article.model.Article;
import club.ttg.dnd5.domain.article.model.ArticleType;
import club.ttg.dnd5.domain.article.repository.ArticleRepository;
import club.ttg.dnd5.domain.article.rest.dto.ArticleDetailedResponse;
import club.ttg.dnd5.domain.article.rest.dto.ArticleRequest;
import club.ttg.dnd5.domain.article.rest.dto.ArticleShortResponse;
import club.ttg.dnd5.domain.article.rest.mapper.ArticleMapper;
import club.ttg.dnd5.domain.revision.model.RevisionOperation;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ArticleService {

    public static final String REVISION_ENTITY_TYPE = "article";

    private static final int DEFAULT_SEARCH_SIZE = 10;
    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final EntityRevisionService revisionService;

    @Transactional
    public String save(ArticleRequest request) {
        validateUrlNonExistence(request.getUrl());

        Article toSave = articleMapper.toEntity(request);
        applyPublication(toSave, request);

        Article saved = articleRepository.save(toSave);
        revisionService.record(REVISION_ENTITY_TYPE, saved.getUrl(), RevisionOperation.CREATE,
                findFormByUrl(saved.getUrl()));
        return saved.getUrl();
    }

    @Transactional
    public String update(String url, ArticleRequest request) {
        Article toUpdate = getByUrl(url);

        if (!url.equals(request.getUrl())) {
            validateUrlNonExistence(request.getUrl());
        }

        articleMapper.updateEntity(toUpdate, request);
        applyPublication(toUpdate, request);

        // Занята под отправку или уже отправлена в канал (и всё ещё помечена к публикации) — пометить на
        // синхронизацию. Учитываем telegramPostedAt (claim), чтобы правка в окне ПЕРВОЙ отправки не потерялась:
        // тогда message_id ещё null, но claim уже стоит, и после отправки поллер синхронизирует свежий текст.
        boolean claimedOrPosted = toUpdate.getTelegramPostedAt() != null || toUpdate.getTelegramMessageId() != null;
        if (claimedOrPosted && toUpdate.isPublishToTelegram()) {
            toUpdate.setTelegramDirty(true);
        }

        String savedUrl = articleRepository.save(toUpdate).getUrl();
        revisionService.record(REVISION_ENTITY_TYPE, savedUrl, RevisionOperation.UPDATE, findFormByUrl(savedUrl));
        return savedUrl;
    }

    /**
     * Переносит флаги публикации в поля сущности.
     * draft=true — черновик: не публична, не активна; дату можно сохранить на будущее.
     * draft=false — опубликована: active=true — активна (будущая дата = запланирована; если дата не задана
     * и её ещё нет — ставим «сейчас»); active=false — неактивна (снята с сайта, дату публикации не трогаем).
     * Черновик и активность/неактивность — независимые оси: неактивная запись остаётся опубликованной.
     */
    private void applyPublication(Article article, ArticleRequest request) {
        if (request.isDraft()) {
            article.setDraft(true);
            article.setActive(false);
            article.setPublishDateTime(request.getPublishDateTime());
            return;
        }
        article.setDraft(false);
        article.setActive(request.isActive());
        if (request.isActive()) {
            if (request.getPublishDateTime() != null) {
                article.setPublishDateTime(request.getPublishDateTime());
            } else if (article.getPublishDateTime() == null) {
                article.setPublishDateTime(Instant.now());
            }
        }
    }

    /**
     * Пытается занять запись под отправку в Telegram (см. {@link ArticleRepository#claimForTelegram}).
     * Короткая отдельная транзакция ДО сетевого вызова.
     *
     * @return {@code true}, если заняли (можно отправлять); {@code false}, если уже занята.
     */
    @Transactional
    public boolean claimTelegramPost(UUID id, Instant when) {
        return articleRepository.claimForTelegram(id, when) == 1;
    }

    /**
     * Освобождает ранее занятую запись после неуспешной отправки — чтобы повторить на следующем тике.
     */
    @Transactional
    public void releaseTelegramPost(UUID id) {
        articleRepository.releaseTelegramClaim(id);
    }

    /**
     * Фиксирует успешную отправку в Telegram: id поста в канале и тип (фото/текст).
     */
    @Transactional
    public void markTelegramSent(UUID id, Long messageId, boolean photo) {
        articleRepository.markTelegramSent(id, messageId, photo);
    }

    /**
     * Снимает флаг правки после синхронизации — только если запись не изменилась с момента загрузки
     * (updatedAt совпадает). Иначе правку, прилетевшую во время отправки, не теряем: флаг остаётся.
     */
    @Transactional
    public void clearTelegramDirty(UUID id, Instant updatedAt) {
        articleRepository.clearTelegramDirtyIfUnchanged(id, updatedAt);
    }

    /**
     * Сбрасывает состояние отправки в Telegram после удаления поста из канала.
     */
    @Transactional
    public void clearTelegramPost(UUID id) {
        articleRepository.clearTelegramPost(id);
    }

    public boolean existsByUrl(String url) {
        return articleRepository.existsByUrl(url);
    }

    public void validateUrlNonExistence(String url) {
        if (existsByUrl(url)) {
            throw new EntityExistException(String.format("Статья / новость с url %s уже существует", url));
        }
    }

    public void validateUrlExistence(String url) {
        if (!existsByUrl(url)) {
            throw new EntityNotFoundException(String.format("Статья / новость с url %s не существует", url));
        }
    }

    public Article getByUrl(String url) {
        return articleRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Статья / новость с url %s не существует", url)));
    }

    /**
     * Публичная выдача: возвращает запись только если она не удалена и уже опубликована.
     */
    public ArticleDetailedResponse findByUrl(String url) {
        Article article = articleRepository.findAccessibleByUrl(url, Instant.now())
                .orElseThrow(() -> new EntityNotFoundException(String.format("Статья / новость с url %s не существует", url)));
        return articleMapper.toDetailedResponse(article);
    }

    public ArticleRequest findFormByUrl(String url) {
        return articleMapper.toRequest(getByUrl(url));
    }

    public ArticleDetailedResponse preview(ArticleRequest request) {
        Article entity = articleMapper.toEntity(request);
        applyPublication(entity, request);
        Instant now = Instant.now();
        entity.setId(new UUID(0L, 0L));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return articleMapper.toDetailedResponse(entity);
    }

    @Transactional
    public String delete(String url) {
        Article toDelete = getByUrl(url);
        toDelete.setDeleted(true);
        String savedUrl = articleRepository.save(toDelete).getUrl();
        revisionService.record(REVISION_ENTITY_TYPE, savedUrl, RevisionOperation.DELETE, findFormByUrl(savedUrl));
        return savedUrl;
    }


    public List<ArticleShortResponse> searchPublished(Integer cnt, ArticleType type, String search) {
        return articleMapper.toShortResponseList(
                articleRepository.findPublished(Instant.now(), type, toLikePattern(search), toLimit(cnt)));
    }

    public List<ArticleShortResponse> searchUnpublished(Integer cnt, ArticleType type, String search) {
        return articleMapper.toShortResponseList(
                articleRepository.findUnpublished(Instant.now(), type, toLikePattern(search), toLimit(cnt)));
    }

    private Limit toLimit(Integer cnt) {
        return Optional.ofNullable(cnt)
                .map(Limit::of)
                .orElseGet(() -> Limit.of(DEFAULT_SEARCH_SIZE));
    }

    /**
     * Готовый LIKE-шаблон в нижнем регистре: "%" — без фильтра (совпадает со всеми заголовками),
     * иначе "%<текст>%". Всегда непустая строка, поэтому параметр биндится как varchar (не bytea).
     */
    private String toLikePattern(String search) {
        if (search == null || search.isBlank()) {
            return "%";
        }
        return "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
