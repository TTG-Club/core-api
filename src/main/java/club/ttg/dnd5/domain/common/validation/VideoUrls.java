package club.ttg.dnd5.domain.common.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ссылки на ролики YouTube и ВК Видео: проверка допустимости и разбор для кросс-постинга.
 * Пустая строка или {@code null} — «видео нет».
 */
public final class VideoUrls {

    public static final int MAX_LENGTH = 512;

    /** Хосты, ролики с которых сайт умеет встраивать плеером. */
    public static final Set<String> ALLOWED_HOSTS = Set.of(
            "youtube.com", "www.youtube.com", "m.youtube.com", "youtu.be",
            "youtube-nocookie.com", "www.youtube-nocookie.com",
            "vk.com", "m.vk.com", "vk.ru", "vkvideo.ru");

    public static final String ALLOWED_MESSAGE = "ссылка на видео должна начинаться с https:// и вести на YouTube "
            + "или ВК Видео (youtube.com, youtu.be, youtube-nocookie.com, vk.com, vk.ru, vkvideo.ru), до "
            + MAX_LENGTH + " символов";

    private static final Set<String> VK_HOSTS = Set.of("vk.com", "m.vk.com", "vk.ru", "vkvideo.ru");

    /** {@code /video<oid>_<id>}: oid отрицательный у роликов сообщества. */
    private static final Pattern VK_VIDEO_PATH = Pattern.compile("^/video(-?\\d+)_(\\d+)/?$");

    private static final Pattern NUMBER = Pattern.compile("^-?\\d+$");

    private VideoUrls() {
    }

    /** Пустая строка → {@code null}, иначе ссылка без пробелов по краям. */
    public static String normalize(String url) {
        return url == null || url.isBlank() ? null : url.strip();
    }

    /**
     * Ссылку можно сохранить: пусто (видео нет) либо {@code https://} на хост из {@link #ALLOWED_HOSTS}
     * длиной не больше {@link #MAX_LENGTH}.
     */
    public static boolean isAllowed(String url) {
        String normalized = normalize(url);
        if (normalized == null) {
            return true;
        }
        if (normalized.length() > MAX_LENGTH) {
            return false;
        }
        return host(normalized, Set.of("https"))
                .map(ALLOWED_HOSTS::contains)
                .orElse(false);
    }

    /**
     * Строка вложения ВКонтакте ({@code video<oid>_<id>}) для роликов ВК Видео:
     * {@code https://vk.com/video<oid>_<id>} и плеер {@code https://vk.com/video_ext.php?oid=…&id=…}.
     * Для YouTube и нераспознанных ссылок — пусто.
     */
    public static Optional<String> vkVideoAttachment(String url) {
        String normalized = normalize(url);
        if (normalized == null || !isAllowed(normalized)) {
            return Optional.empty();
        }
        URI uri = URI.create(normalized);
        if (!VK_HOSTS.contains(uri.getHost().toLowerCase(Locale.ROOT)) || uri.getPath() == null) {
            return Optional.empty();
        }
        Matcher path = VK_VIDEO_PATH.matcher(uri.getPath());
        if (path.matches()) {
            return Optional.of("video" + path.group(1) + "_" + path.group(2));
        }
        if ("/video_ext.php".equals(uri.getPath()) && uri.getRawQuery() != null) {
            String oid = null;
            String id = null;
            for (String pair : uri.getRawQuery().split("&")) {
                int eq = pair.indexOf('=');
                if (eq < 0) {
                    continue;
                }
                String key = pair.substring(0, eq);
                String value = pair.substring(eq + 1);
                if ("oid".equals(key)) {
                    oid = value;
                } else if ("id".equals(key)) {
                    id = value;
                }
            }
            if (oid != null && id != null && NUMBER.matcher(oid).matches() && NUMBER.matcher(id).matches()) {
                return Optional.of("video" + oid + "_" + id);
            }
        }
        return Optional.empty();
    }

    /**
     * Обычная веб-ссылка: {@code http://} или {@code https://} с хостом, длиной не больше {@code maxLength}.
     * Пусто — допустимо (ссылки нет).
     */
    public static boolean isWebUrl(String url, int maxLength) {
        String normalized = normalize(url);
        if (normalized == null) {
            return true;
        }
        return normalized.length() <= maxLength && host(normalized, Set.of("http", "https")).isPresent();
    }

    /** Хост ссылки в нижнем регистре, если она разбирается и схема из {@code schemes}; ссылки с логином отвергаем. */
    private static Optional<String> host(String url, Set<String> schemes) {
        try {
            URI uri = new URI(url);
            if (uri.getScheme() == null || !schemes.contains(uri.getScheme().toLowerCase(Locale.ROOT))
                    || uri.getHost() == null || uri.getUserInfo() != null) {
                return Optional.empty();
            }
            return Optional.of(uri.getHost().toLowerCase(Locale.ROOT));
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }
}
