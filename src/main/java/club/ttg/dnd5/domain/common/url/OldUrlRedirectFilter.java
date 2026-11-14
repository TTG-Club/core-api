package club.ttg.dnd5.domain.common.url;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Прежние адреса записей продолжают работать.
 *
 * <p>Запрос к записи каталога по адресу, который числится прежним
 * ({@code GET /api/v2/spells/fireball-phb}, а также {@code /raw}, {@code HEAD} и прочие
 * ручки вида {@code /api/v2/{раздел}/{url}…}), обрабатывается так, будто пришёл по
 * текущему адресу записи: ответ — обычная запись с её <b>новым</b> {@code url} в теле.
 * Вдобавок текущий адрес отдаётся в заголовке {@value #HEADER} — сайт по нему делает
 * перенаправление.</p>
 *
 * <p>Прежний адрес по построению не принадлежит ни одной живой записи
 * ({@link CatalogUrlGuard}), поэтому подмена не может увести запрос с существующей записи.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class OldUrlRedirectFilter extends OncePerRequestFilter {
    /** Заголовок ответа с текущим адресом записи. */
    public static final String HEADER = "X-Entity-Url";

    /** Первый сегмент пути API → раздел. */
    private static final Map<String, String> PATH_SECTIONS = Map.ofEntries(
            Map.entry("spells", "spells"),
            Map.entry("bestiary", "bestiary"),
            Map.entry("classes", "classes"),
            Map.entry("species", "species"),
            Map.entry("feats", "feats"),
            Map.entry("backgrounds", "backgrounds"),
            Map.entry("items", "items"),
            Map.entry("item", "items"),
            Map.entry("magic-items", "magic-items"),
            Map.entry("glossary", "glossary"),
            Map.entry("bastions", "bastions"));

    private static final Pattern PATH = Pattern.compile("^(/api/v2/)([^/]+)/([^/]+)(/.*)?$");

    private final EntityUrlHistoryRegistry history;

    public OldUrlRedirectFilter(EntityUrlHistoryRegistry history) {
        this.history = history;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Optional<Rewrite> rewrite = rewrite(request.getRequestURI(), request.getContextPath());
        if (rewrite.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }
        response.setHeader(HEADER, rewrite.get().url());
        chain.doFilter(new RewrittenRequest(request, rewrite.get()), response);
    }

    /** Подмена пути, если адрес в нём числится прежним. */
    Optional<Rewrite> rewrite(String requestUri, String contextPath) {
        String context = contextPath == null ? "" : contextPath;
        if (requestUri == null || !requestUri.startsWith(context)) {
            return Optional.empty();
        }
        Matcher matcher = PATH.matcher(requestUri.substring(context.length()));
        if (!matcher.matches()) {
            return Optional.empty();
        }
        String section = PATH_SECTIONS.get(matcher.group(2));
        if (section == null) {
            return Optional.empty();
        }
        String rest = matcher.group(4) == null ? "" : matcher.group(4);
        return history.currentUrl(section, matcher.group(3))
                .map(url -> new Rewrite(url,
                        context + matcher.group(1) + matcher.group(2) + "/" + url + rest,
                        matcher.group(1) + matcher.group(2) + "/" + url + rest));
    }

    /**
     * @param url         текущий адрес записи
     * @param requestUri  новый путь с контекстом приложения
     * @param servletPath новый путь без контекста
     */
    record Rewrite(String url, String requestUri, String servletPath) {
    }

    private static final class RewrittenRequest extends HttpServletRequestWrapper {
        private final Rewrite rewrite;

        RewrittenRequest(HttpServletRequest request, Rewrite rewrite) {
            super(request);
            this.rewrite = rewrite;
        }

        @Override
        public String getRequestURI() {
            return rewrite.requestUri();
        }

        @Override
        public StringBuffer getRequestURL() {
            StringBuffer url = new StringBuffer();
            url.append(getScheme()).append("://").append(getServerName());
            int port = getServerPort();
            if (port > 0 && !(port == 80 && "http".equals(getScheme())) && !(port == 443 && "https".equals(getScheme()))) {
                url.append(':').append(port);
            }
            return url.append(rewrite.requestUri());
        }

        @Override
        public String getServletPath() {
            return rewrite.servletPath();
        }

        @Override
        public String getPathInfo() {
            return null;
        }
    }
}
