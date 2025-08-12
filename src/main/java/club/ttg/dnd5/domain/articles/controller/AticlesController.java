package club.ttg.dnd5.domain.articles.controller;

import club.ttg.dnd5.domain.articles.rest.dto.ArticleDetailedResponse;
import club.ttg.dnd5.domain.articles.rest.dto.ArticleShortResponse;
import club.ttg.dnd5.domain.articles.rest.dto.create.ArticleRequest;
import club.ttg.dnd5.domain.articles.service.ArticleService;
import club.ttg.dnd5.domain.articles.service.ArticlesFilterServise;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Статьи", description = "REST API глоссарий")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/articles")
public class AticlesController {
    private final ArticleService articleService;
    private final ArticlesFilterServise articleFilterService;

    @Operation(summary = "Проверить статью по URL", description = "Проверка статьи по ее уникальному URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статьи существует"),
            @ApiResponse(responseCode = "404", description = "Статьи не существует")
    })
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public Boolean isArticlesExist(@PathVariable String url) {
        return articleService.existOrThrow(url);
    }

    @Operation(summary = "Поиск статей", description = "Поиск статьй по именам")
    @PostMapping("/search")
    public List<ArticleShortResponse> search(@RequestParam(name = "query", required = false)
                                              @Valid
                                              @Size(min = 3)
                                              @Schema( description = "Строка поиска, если null-отдаются все сущности")
                                              String searchLine,
                                              @RequestBody(required = false) SearchBody searchBody) {
        return articleService.search(searchLine, searchBody);
    }

    @GetMapping("/{url}")
    public ArticleDetailedResponse getArticlesByUrl(@PathVariable String url) {
        return articleService.findDetailedByUrl(url);
    }

    @GetMapping("/{url}/raw")
    public ArticleRequest getArticlesFormByUrl(@PathVariable String url) {
        return articleService.findFormByUrl(url);
    }

    @GetMapping("/filters")
    public FilterInfo getFilters() {
        return articleFilterService.getDefaultFilterInfo();
    }

    @Secured("ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String create(@RequestBody ArticleRequest request) {
        return articleService.save(request);
    }

    @Secured("ADMIN")
    @PutMapping("/{url}")
    public String updateArticles(@PathVariable String url,
                                             @Valid
                                             @RequestBody ArticleRequest request) {
        return articleService.update(url, request);
    }

    @Secured("ADMIN")
    @DeleteMapping("/{url}")
    public void deleteArticles(@PathVariable String url) {
        articleService.delete(url);
    }
}
