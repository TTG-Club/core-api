package club.ttg.dnd5.domain.article.rest.controller;

import club.ttg.dnd5.domain.article.service.ArticleInstantViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Страницы записей для Telegram Instant View.
 * <p>
 * Путь вне {@code /api} — адрес виден читателю (кнопка «открыть в браузере» в Instant View), поэтому
 * он короткий: {@code https://<сайт>/iv/articles/<slug>}. На домене сайта запросы {@code /iv/**}
 * проксирует фронт в core-api (core-app: {@code server/routes/iv/[...].get.ts}).
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/iv/articles")
@Tag(name = "Instant View", description = "HTML-страницы статей / новостей для Telegram Instant View")
public class ArticleInstantViewController {

    private final ArticleInstantViewService instantViewService;

    @Operation(summary = "HTML-страница статьи / новости для Telegram Instant View")
    @GetMapping(path = "/{url}", produces = "text/html;charset=UTF-8")
    public String page(@PathVariable final String url) {
        return instantViewService.renderPage(url);
    }
}
