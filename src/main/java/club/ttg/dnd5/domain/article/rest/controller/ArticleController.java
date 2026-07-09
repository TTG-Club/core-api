package club.ttg.dnd5.domain.article.rest.controller;

import club.ttg.dnd5.domain.article.model.ArticleType;
import club.ttg.dnd5.domain.article.rest.dto.ArticleDetailedResponse;
import club.ttg.dnd5.domain.article.rest.dto.ArticleRequest;
import club.ttg.dnd5.domain.article.rest.dto.ArticleShortResponse;
import club.ttg.dnd5.domain.article.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/articles")
@Tag(name = "Статьи / Новости", description = "REST API статей / новостей")
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "Проверка занятости url (для админки при выборе slug): "
            + "200 — url уже занят (в т.ч. черновиком/удалённой/отложенной), 404 — свободен")
    @Secured({"ADMIN", "MODERATOR"})
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public boolean existByUrl(@PathVariable final String url) {
        articleService.validateUrlExistence(url);
        return true;
    }

    @Operation(summary = "Полная статья / новость по url")
    @GetMapping("/{url}")
    public ArticleDetailedResponse findByUrl(@PathVariable final String url) {
        return articleService.findByUrl(url);
    }

    @Operation(summary = "Создание статьи / новости")
    @Secured({"ADMIN", "MODERATOR"})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public String addArticle(@RequestBody @Valid final ArticleRequest request) {
        return articleService.save(request);
    }

    @Operation(summary = "Обновление статьи / новости")
    @Secured({"ADMIN", "MODERATOR"})
    @PutMapping("/{url}")
    public String updateArticle(@PathVariable final String url, @RequestBody @Valid final ArticleRequest request) {
        return articleService.update(url, request);
    }

    @Operation(summary = "Полная форма для редактирования статьи / новости по url")
    @Secured({"ADMIN", "MODERATOR"})
    @GetMapping("/{url}/raw")
    public ArticleRequest getRawByUrl(@PathVariable final String url) {
        return articleService.findFormByUrl(url);
    }

    @Operation(summary = "Предпросмотр статьи / новости")
    @Secured({"ADMIN", "MODERATOR"})
    @PostMapping("/preview")
    public ArticleDetailedResponse preview(@RequestBody @Valid final ArticleRequest request) {
        return articleService.preview(request);
    }

    @Operation(summary = "Помечает статью / новость как скрытую для списков")
    @Secured({"ADMIN", "MODERATOR"})
    @DeleteMapping("/{url}")
    public String deleteArticle(@PathVariable final String url) {
        return articleService.delete(url);
    }

    @Operation(summary = "Получить cnt последних опубликованных статей / новостей (с фильтром по типу и поиском)")
    @GetMapping("/search")
    public List<ArticleShortResponse> search(
            @RequestParam(required = false)
            @Schema(description = "Сколько статей / новостей грузить (дефолт - 10)") final Integer cnt,
            @RequestParam(required = false)
            @Schema(description = "Фильтр по типу: NEWS или ARTICLE (по умолчанию — все)") final ArticleType type,
            @RequestParam(required = false)
            @Schema(description = "Поиск по подстроке в заголовке (регистронезависимо)") final String search) {
        return articleService.searchPublished(cnt, type, search);
    }

    @Operation(summary = "Получить cnt последних неопубликованных статей / новостей (для модерации, с фильтром и поиском)")
    @GetMapping("/search/unpublished")
    @Secured({"ADMIN", "MODERATOR"})
    public List<ArticleShortResponse> searchUnpublished(
            @RequestParam(required = false)
            @Schema(description = "Сколько статей / новостей грузить (дефолт - 10)") final Integer cnt,
            @RequestParam(required = false)
            @Schema(description = "Фильтр по типу: NEWS или ARTICLE (по умолчанию — все)") final ArticleType type,
            @RequestParam(required = false)
            @Schema(description = "Поиск по подстроке в заголовке (регистронезависимо)") final String search) {
        return articleService.searchUnpublished(cnt, type, search);
    }
}
