package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.domain.article.model.Article;
import club.ttg.dnd5.domain.article.repository.ArticleRepository;
import club.ttg.dnd5.domain.article.rest.dto.ArticleDetailedResponse;
import club.ttg.dnd5.domain.article.rest.dto.ArticleRequest;
import club.ttg.dnd5.domain.article.rest.dto.ArticleShortResponse;
import club.ttg.dnd5.domain.article.rest.mapper.ArticleMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ArticleService {

    private static final int DEFAULT_SEARCH_SIZE = 10;
    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;

    public String save(ArticleRequest request) {
        validateUrlNonExistence(request.getUrl());

        Article toSave = articleMapper.toEntity(request);

        return articleRepository.save(toSave).getUrl();
    }

    public String update(UUID id, ArticleRequest request) {
        Article toUpdate = getById(id);

        articleMapper.updateEntity(toUpdate, request);

        return articleRepository.save(toUpdate).getUrl();
    }

    public boolean existsByUrl(String url) {
        return articleRepository.existsByUrl(url);
    }

    public void validateUrlNonExistence(String url) {
        if (existsByUrl(url)) {
            throw new EntityExistException(String.format("Статья с url %s уже существует", url));
        }
    }

    public void validateUrlExistence(String url) {
        if (!existsByUrl(url)) {
            throw new EntityNotFoundException(String.format("Статья с url %s не существует", url));
        }
    }

    public Article getByUrl(String url) {
        return articleRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Статья с url %s не существует", url)));
    }

    public Article getById(UUID id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Статья с id %s не существует", id)));
    }

    public ArticleDetailedResponse findByUrl(String url) {
        return articleMapper.toDetailedResponse(getByUrl(url));
    }

    public ArticleRequest findFormById(UUID id) {
        return articleMapper.toRequest(getById(id));
    }

    public ArticleDetailedResponse preview(ArticleRequest request) {
        return articleMapper.toDetailedResponse(articleMapper.toEntity(request));
    }

    public String delete(UUID id) {
        Article toDelete = getById(id);
        toDelete.setDeleted(true);
        return articleRepository.save(toDelete).getUrl();
    }


    public List<ArticleShortResponse> searchPublished(Integer cnt) {
        return articleMapper.toShortResponseList(articleRepository.findAllByDeletedFalseAndPublishDateTimeBeforeOrderByPublishDateTimeDesc(
                Instant.now(),
                Optional.ofNullable(cnt)
                        .map(Limit::of)
                        .orElseGet(() -> Limit.of(DEFAULT_SEARCH_SIZE))));
    }

    public List<ArticleShortResponse> searchUnpublished(Integer cnt) {
        return articleMapper.toShortResponseList(articleRepository.findAllByDeletedFalseOrderByCreatedAtDesc(Optional.ofNullable(cnt)
                .map(Limit::of)
                .orElseGet(() -> Limit.of(DEFAULT_SEARCH_SIZE))));
    }
}
