package club.ttg.dnd5.domain.articles.service;

import club.ttg.dnd5.domain.articles.model.Article;
import club.ttg.dnd5.domain.articles.repository.ArticleRepository;
import club.ttg.dnd5.domain.articles.rest.dto.ArticleDetailedResponse;
import club.ttg.dnd5.domain.articles.rest.dto.ArticleShortResponse;
import club.ttg.dnd5.domain.articles.rest.dto.create.ArticleRequest;
import club.ttg.dnd5.domain.articles.rest.mapper.ArticleMapper;
import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.service.BookService;
import club.ttg.dnd5.domain.common.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final BookService bookService;
    private final ArticleMapper articleMapper;
    private final ArticleQueryDslSearchService articleQueryDslSearchService;

    public List<ArticleShortResponse> search(String searchLine, SearchBody searchBody) {
        return articleQueryDslSearchService.search(searchLine, null).stream()
                .filter(article -> article.getCategories() != null && article.getCategories().contains("articles"))
                .map(articleMapper::toShort)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(cacheNames = "countAllMaterials")
    public String save(ArticleRequest articleRequest) {
        if (articleRepository.existsById(articleRequest.getUrl())) {
            throw new EntityExistException(String.format("Article with url %s already exists", articleRequest.getUrl()));
        }

        Book book = Optional.ofNullable(articleRequest.getSource())
                .map(SourceRequest::getUrl)
                .map(bookService::findByUrl)
                .orElse(null);

        Article article = articleMapper.toEntity(articleRequest, book);
        article = articleRepository.save(article);

        return article.getUrl();
    }

    @Transactional
    public String update(String url, ArticleRequest request) {
        Article existingGlossary = articleRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Article with url %s not found", url)));

        Book book = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(bookService::findByUrl)
                .orElse(null);

        Article updatedArticle = articleMapper.toEntity(request, book);
        updatedArticle.setUrl(url);
        articleRepository.delete(existingGlossary);
        articleRepository.save(updatedArticle);

        return updatedArticle.getUrl();
    }

    @Transactional
    @CacheEvict(cacheNames = "countAllMaterials")
    public void delete(String url) {
        Article existingArticle = articleRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Article with url %s not found", url)));

        existingArticle.setHiddenEntity(true);
        articleRepository.save(existingArticle);
    }

    public ArticleDetailedResponse findByUrl(String url) {
        Article article = articleRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Article with url %s not found", url)));

        return articleMapper.toDetail(article);
    }

    public boolean existOrThrow(String url) {
        if (!articleRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Article with url %s does not exist", url));
        }
        return true;
    }

    public ArticleDetailedResponse findDetailedByUrl(String url) {
        Article article = articleRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Article with url %s not found", url)));

        return articleMapper.toDetail(article);
    }

    public ArticleRequest findFormByUrl(String url) {
        Article article = articleRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Article with url %s not found", url)));

        return articleMapper.toRequest(article);
    }
}
