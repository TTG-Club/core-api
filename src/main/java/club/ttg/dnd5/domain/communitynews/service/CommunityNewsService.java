package club.ttg.dnd5.domain.communitynews.service;

import club.ttg.dnd5.domain.communitynews.model.CommunityNews;
import club.ttg.dnd5.domain.communitynews.repository.CommunityNewsRepository;
import club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsDetailedResponse;
import club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsRequest;
import club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsShortResponse;
import club.ttg.dnd5.domain.communitynews.rest.mapper.CommunityNewsMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Новости сообщества. Семантика публикации — как у статей ({@code ArticleService}): черновик,
 * активна / снята, отложенная дата, доступ по прямой ссылке, мягкое удаление.
 */
@RequiredArgsConstructor
@Service
public class CommunityNewsService {

    private static final int DEFAULT_SEARCH_SIZE = 10;

    private final CommunityNewsRepository repository;
    private final CommunityNewsMapper mapper;

    @Transactional
    public String save(CommunityNewsRequest request) {
        validateUrlNonExistence(request.getUrl());
        CommunityNews news = mapper.toEntity(request);
        applyPublication(news, request);
        return repository.save(news).getUrl();
    }

    @Transactional
    public String update(String url, CommunityNewsRequest request) {
        CommunityNews news = getByUrl(url);
        if (!url.equals(request.getUrl())) {
            validateUrlNonExistence(request.getUrl());
        }
        mapper.updateEntity(news, request);
        applyPublication(news, request);
        return repository.save(news).getUrl();
    }

    @Transactional
    public String delete(String url) {
        CommunityNews news = getByUrl(url);
        news.setDeleted(true);
        return repository.save(news).getUrl();
    }

    /** Публичная выдача: активная и вышедшая запись либо неактивная, открытая по прямой ссылке. */
    public CommunityNewsDetailedResponse findByUrl(String url) {
        return repository.findAccessibleByUrl(url, Instant.now())
                .map(mapper::toDetailedResponse)
                .orElseThrow(() -> notFound(url));
    }

    public CommunityNewsRequest findFormByUrl(String url) {
        return mapper.toRequest(getByUrl(url));
    }

    /** Предпросмотр без сохранения: та же сборка, что при сохранении, с фиктивными id и датами. */
    public CommunityNewsDetailedResponse preview(CommunityNewsRequest request) {
        CommunityNews news = mapper.toEntity(request);
        applyPublication(news, request);
        Instant now = Instant.now();
        news.setId(new UUID(0L, 0L));
        news.setCreatedAt(now);
        news.setUpdatedAt(now);
        return mapper.toDetailedResponse(news);
    }

    /** Последние вышедшие записи, свежие сверху; {@code search} — подстрока заголовка без учёта регистра. */
    public List<CommunityNewsShortResponse> searchPublished(Integer cnt, String search) {
        return mapper.toShortResponseList(
                repository.findPublished(Instant.now(), toLikePattern(search), toLimit(cnt)));
    }

    /** Черновики, снятые и запланированные одним списком (для редакторов). */
    public List<CommunityNewsShortResponse> searchUnpublished(Integer cnt, String search) {
        return mapper.toShortResponseList(
                repository.findUnpublished(Instant.now(), toLikePattern(search), toLimit(cnt)));
    }

    public void validateUrlExistence(String url) {
        if (!repository.existsByUrl(url)) {
            throw notFound(url);
        }
    }

    private void validateUrlNonExistence(String url) {
        if (repository.existsByUrl(url)) {
            throw new EntityExistException(String.format("Новость сообщества с url %s уже существует", url));
        }
    }

    private CommunityNews getByUrl(String url) {
        return repository.findByUrl(url).orElseThrow(() -> notFound(url));
    }

    private static EntityNotFoundException notFound(String url) {
        return new EntityNotFoundException(String.format("Новость сообщества с url %s не существует", url));
    }

    /**
     * Флаги публикации — как у статей. draft=true: черновик, не активна, дату можно сохранить на будущее.
     * draft=false: опубликована; active=true — в общем доступе (будущая дата — запланирована, без даты —
     * «сейчас», если даты ещё не было); active=false — снята с сайта, дату не трогаем.
     */
    static void applyPublication(CommunityNews news, CommunityNewsRequest request) {
        if (request.isDraft()) {
            news.setDraft(true);
            news.setActive(false);
            news.setPublishDateTime(request.getPublishDateTime());
            return;
        }
        news.setDraft(false);
        news.setActive(request.isActive());
        if (request.isActive()) {
            if (request.getPublishDateTime() != null) {
                news.setPublishDateTime(request.getPublishDateTime());
            } else if (news.getPublishDateTime() == null) {
                news.setPublishDateTime(Instant.now());
            }
        }
    }

    private static Limit toLimit(Integer cnt) {
        return Limit.of(cnt == null || cnt <= 0 ? DEFAULT_SEARCH_SIZE : cnt);
    }

    /** LIKE-шаблон в нижнем регистре: "%" — без фильтра, иначе "%текст%". Всегда непустой (не bytea). */
    private static String toLikePattern(String search) {
        if (search == null || search.isBlank()) {
            return "%";
        }
        return "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
