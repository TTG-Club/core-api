package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.domain.article.model.Article;
import club.ttg.dnd5.domain.article.repository.ArticleRepository;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Отдаёт статью / новость отдельной HTML-страницей для Telegram Instant View.
 * <p>
 * Зачем отдельная страница: на сайте текст записи рисуется на клиенте (MarkupRender обёрнут в
 * {@code <ClientOnly>}), а робот Instant View скачивает HTML и JS не исполняет — по странице сайта
 * шаблон IV собрать нельзя. Здесь разметка разворачивается в простой семантический HTML со
 * СТАБИЛЬНЫМИ якорями ({@code article}, {@code h1}, {@code img.cover}, {@code time}), по которым
 * пишется шаблон на instantview.telegram.org (см. {@code docs/telegram-instant-view.md}). Хеши
 * классов Nuxt меняются от сборки к сборке, поэтому цеплять шаблон к странице сайта нельзя.
 * <p>
 * Страница служебная: канонический адрес — статья на сайте, индексация закрыта ({@code noindex}).
 */
@RequiredArgsConstructor
@Service
public class ArticleInstantViewService {

    /** Видимая дата под заголовком. Точное время Telegram берёт из {@code <time datetime>}. */
    private static final DateTimeFormatter VISIBLE_DATE =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.of("ru"));
    /** Часовой пояс проекта — в нём показываем дату публикации. */
    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");
    private static final String SITE_NAME = "TTG Club";

    private final ArticleRepository articleRepository;
    private final InstantViewHtmlFormatter formatter;

    @Value("${app.url:https://ttg.club}")
    private String appUrl;

    /**
     * HTML-страница записи для Instant View. Видимость — как у публичной выдачи
     * ({@link ArticleRepository#findAccessibleByUrl}): черновик, отложенная и удалённая недоступны.
     */
    public String renderPage(String url) {
        Article article = articleRepository.findAccessibleByUrl(url, Instant.now())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Статья / новость с url %s не существует", url)));
        return render(article);
    }

    private String render(Article article) {
        String title = nullToEmpty(article.getTitle());
        String canonical = site() + "/articles/" + article.getUrl();
        String pageUrl = site() + "/iv/articles/" + article.getUrl();
        String cover = absolute(article.getPreviewImageUrl());
        String lead = formatter.toHtml(article.getPreview());
        String content = formatter.toHtml(article.getContent());
        String description = shorten(formatter.toPlain(
                StringUtils.hasText(formatter.toPlain(article.getPreview()))
                        ? article.getPreview()
                        : article.getContent()));

        StringBuilder html = new StringBuilder(1024);
        html.append("<!DOCTYPE html><html lang=\"ru\"><head>")
                .append("<meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                // Служебная страница-двойник статьи: в поиске не нужна, вес отдаём сайту.
                .append("<meta name=\"robots\" content=\"noindex, follow\">")
                .append("<title>").append(escape(title)).append(" | ").append(SITE_NAME).append("</title>")
                .append("<link rel=\"canonical\" href=\"").append(escape(canonical)).append("\">")
                .append(meta("og:type", "article"))
                .append(meta("og:site_name", SITE_NAME))
                .append(meta("og:title", title))
                .append(meta("og:description", description))
                .append(meta("og:url", pageUrl));
        if (cover != null) {
            html.append(meta("og:image", cover));
        }
        if (article.getPublishDateTime() != null) {
            html.append(meta("article:published_time", article.getPublishDateTime().toString()));
        }
        html.append(style())
                .append("</head><body><article>")
                .append("<h1>").append(escape(title)).append("</h1>");
        if (article.getPublishDateTime() != null) {
            html.append("<time datetime=\"").append(article.getPublishDateTime().toString()).append("\">")
                    .append(VISIBLE_DATE.format(article.getPublishDateTime().atZone(ZONE)))
                    .append("</time>");
        }
        if (cover != null) {
            html.append("<img class=\"cover\" src=\"").append(escape(cover))
                    .append("\" alt=\"").append(escape(title)).append("\">");
        }
        if (StringUtils.hasText(lead)) {
            html.append("<div class=\"lead\">").append(lead).append("</div>");
        }
        if (StringUtils.hasText(content)) {
            html.append("<div class=\"content\">").append(content).append("</div>");
        }
        html.append("<p class=\"source\"><a href=\"").append(escape(canonical)).append("\">Читать на сайте</a></p>")
                .append("</article></body></html>");
        return html.toString();
    }

    /** Минимальное оформление: страницу открывают и в обычном браузере (кнопка «в браузере» в IV). */
    private static String style() {
        return "<style>"
                + "body{margin:0 auto;padding:16px;max-width:44rem;"
                + "font:16px/1.6 system-ui,-apple-system,\"Segoe UI\",Roboto,sans-serif;}"
                + "img{max-width:100%;height:auto;border-radius:12px;}"
                + "time{color:#777;font-size:.875rem;}"
                + "blockquote{margin:1rem 0;padding:.25rem 1rem;border-left:3px solid #bbb;color:#555;}"
                + "table{width:100%;border-collapse:collapse;}"
                + "th,td{padding:.25rem .5rem;border:1px solid #ddd;text-align:left;}"
                + "</style>";
    }

    private static String meta(String property, String content) {
        return "<meta property=\"" + property + "\" content=\"" + escape(nullToEmpty(content)) + "\">";
    }

    /**
     * Абсолютный URL обложки: {@code previewImageUrl} хранится относительным ({@code /s3/<key>}),
     * а Telegram качает картинку сам. {@code null} — обложки нет.
     */
    private String absolute(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return null;
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }
        return imageUrl.startsWith("/") ? site() + imageUrl : site() + "/" + imageUrl;
    }

    /**
     * Публичная база адресов. Страницу читает Telegram, поэтому не-публичную базу
     * (localhost/без схемы — локальная разработка) подменяем боевым сайтом.
     */
    private String site() {
        String url = appUrl == null ? "" : appUrl.trim();
        boolean usable = (url.startsWith("http://") || url.startsWith("https://")) && !url.contains("localhost");
        if (!usable) {
            url = "https://ttg.club";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /** Описание для og-разметки: одна-две фразы, длинное режем по границе слова. */
    private static String shorten(String text) {
        if (text.length() <= 300) {
            return text;
        }
        int cut = text.lastIndexOf(' ', 300);
        return text.substring(0, cut > 0 ? cut : 300).strip() + "…";
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String escape(String value) {
        return nullToEmpty(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
