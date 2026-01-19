package club.ttg.dnd5.domain.article.rest.controller;


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
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/articles")
@Tag(name = "Новости", description = "REST API новостей")
public class ArticleController {

    private final ArticleService articleService;

    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public boolean existByUrl(@PathVariable final String url) {
        articleService.validateUrlExistence(url);
        return true;
    }

    @Operation(summary = "Полный объект новости по url")
    @GetMapping("/{url}")
    public ArticleDetailedResponse findByUrl(@PathVariable final String url) {
        return articleService.findByUrl(url);
    }

    @Operation(summary = "Создание новости")
    @Secured("ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public String addArticle(@RequestBody @Valid final ArticleRequest request) {
        return articleService.save(request);
    }

    @Operation(summary = "Обновление новости")
    @Secured("ADMIN")
    @PutMapping("/{id}")
    public String updateArticle(@PathVariable final UUID id, @RequestBody final ArticleRequest request) {
        return articleService.update(id, request);
    }

    @Operation(summary = "Полный форма для редактирования новости по id")
    @GetMapping("/{id}/raw")
    public ArticleRequest getById(@PathVariable final UUID id) {
        return articleService.findFormById(id);
    }

    @Operation(summary = "Предпросмотр новости")
    @Secured("ADMIN")
    @PostMapping("/preview")
    public ArticleDetailedResponse preview(@RequestBody @Valid final ArticleRequest request) {
        return articleService.preview(request);
    }

    @Operation(summary = "Помечает новость как скрытую для списков")
    @Secured("ADMIN")
    @DeleteMapping("{id}")
    public String deleteArticle(@PathVariable final UUID id) {
        return articleService.delete(id);
    }

    @Operation(summary = "Получить cnt последних новостей")
    @GetMapping("/search")
    public List<ArticleShortResponse> search(@RequestParam(required = false) @Schema(description = "Сколько новостей грузить (дефолт - 10)") final Integer cnt) {
        return articleService.searchPublished(cnt);
    }

    @Operation(summary = "Получить cnt последних новостей")
    @GetMapping("/search/unpublished")
    @Secured("ADMIN")
    public List<ArticleShortResponse> searchUnpublished(@RequestParam(required = false) @Schema(description = "Сколько новостей грузить (дефолт - 10)") final Integer cnt) {
        return articleService.searchUnpublished(cnt);
    }

}
