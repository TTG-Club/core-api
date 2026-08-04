package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.domain.article.model.Article;
import club.ttg.dnd5.domain.article.model.ArticleType;
import club.ttg.dnd5.domain.article.repository.ArticleRepository;
import club.ttg.dnd5.domain.vttg.service.VttgMarkupConverter;
import club.ttg.dnd5.exception.EntityNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Страница Instant View. Проверяем ровно те якоря, на которые опирается шаблон на
 * instantview.telegram.org (см. {@code docs/telegram-instant-view.md}): {@code article}, {@code h1},
 * {@code img.cover}, {@code time/@datetime}, {@code div.lead}. Если разметка страницы поедет,
 * шаблон перестанет собирать статью — тест ловит это до деплоя.
 */
class ArticleInstantViewServiceTest {

    private final ArticleRepository articleRepository = mock(ArticleRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ArticleInstantViewService service = new ArticleInstantViewService(
            articleRepository, new InstantViewHtmlFormatter(new VttgMarkupConverter(objectMapper), objectMapper));

    @Test
    void pageKeepsAnchorsRequiredByTemplate() {
        when(articleRepository.findAccessibleByUrl(eq("obnovlenie-vttg"), any()))
                .thenReturn(Optional.of(article()));

        String page = service.renderPage("obnovlenie-vttg");

        assertTrue(page.contains("<article>"), page);
        assertTrue(page.contains("<h1>Обновление VTTG 0.9.309</h1>"), page);
        assertTrue(page.contains("<time datetime=\"2026-08-04T08:02:31Z\">4 августа 2026</time>"), page);
        assertTrue(page.contains("<img class=\"cover\" src=\"https://ttg.club/s3/articles/cover.webp\""), page);
        assertTrue(page.contains("<div class=\"lead\"><p>Патч после открытия раннего доступа.</p></div>"), page);
    }

    @Test
    void pageRendersContentBlocks() {
        when(articleRepository.findAccessibleByUrl(eq("obnovlenie-vttg"), any()))
                .thenReturn(Optional.of(article()));

        String page = service.renderPage("obnovlenie-vttg");

        assertTrue(page.contains("<h4>Компендиум</h4>"), page);
        assertTrue(page.contains("<ul><li>Объединённый поиск</li></ul>"), page);
    }

    @Test
    void pagePointsToSiteAndStaysOutOfSearch() {
        when(articleRepository.findAccessibleByUrl(eq("obnovlenie-vttg"), any()))
                .thenReturn(Optional.of(article()));

        String page = service.renderPage("obnovlenie-vttg");

        // Служебный двойник статьи: вес отдаём странице сайта, туда же ведёт ссылка со страницы.
        assertTrue(page.contains("<meta name=\"robots\" content=\"noindex, follow\">"), page);
        assertTrue(page.contains("<link rel=\"canonical\" href=\"https://ttg.club/articles/obnovlenie-vttg\">"), page);
        assertTrue(page.contains("<a href=\"https://ttg.club/articles/obnovlenie-vttg\">Читать на сайте</a>"), page);
        // Обложку и заголовок карточки в Telegram берёт из og-разметки.
        assertTrue(page.contains("<meta property=\"og:title\" content=\"Обновление VTTG 0.9.309\">"), page);
        assertTrue(page.contains(
                "<meta property=\"og:image\" content=\"https://ttg.club/s3/articles/cover.webp\">"), page);
        assertTrue(page.contains(
                "<meta property=\"og:description\" content=\"Патч после открытия раннего доступа.\">"), page);
    }

    @Test
    void hiddenArticleIsNotFound() {
        when(articleRepository.findAccessibleByUrl(eq("draft"), any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.renderPage("draft"));
    }

    private static Article article() {
        Article article = new Article();
        article.setUrl("obnovlenie-vttg");
        article.setType(ArticleType.NEWS);
        article.setTitle("Обновление VTTG 0.9.309");
        article.setPreviewImageUrl("/s3/articles/cover.webp");
        article.setPublishDateTime(Instant.parse("2026-08-04T08:02:31Z"));
        article.setPreview("[\"Патч после открытия раннего доступа.\"]");
        article.setContent("""
                [{"type": "heading", "attrs": {"level": 3}, "content": [{"type": "text", "text": "Компендиум"}]},
                 {"type": "list", "content": [
                    {"type": "li", "content": [{"type": "text", "text": "Объединённый поиск"}]}]}]
                """);
        return article;
    }
}
