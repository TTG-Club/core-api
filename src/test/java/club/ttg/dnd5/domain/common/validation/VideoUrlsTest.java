package club.ttg.dnd5.domain.common.validation;

import club.ttg.dnd5.domain.article.model.ArticleType;
import club.ttg.dnd5.domain.article.rest.dto.ArticleRequest;
import club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ссылки на ролики и источник: что пропускаем, что отвергаем 400-й и как разбираем ВК Видео. */
class VideoUrlsTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=42s",
            "https://youtube.com/watch?v=dQw4w9WgXcQ",
            "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ",
            "https://youtube-nocookie.com/embed/dQw4w9WgXcQ",
            "https://vk.com/video-123_456",
            "https://m.vk.com/video123_456",
            "https://vk.ru/video-123_456",
            "https://vkvideo.ru/video-123_456",
            "https://vk.com/video_ext.php?oid=-123&id=456&hash=abc",
            "HTTPS://WWW.YOUTUBE.COM/watch?v=x"
    })
    void allowsEmptyAndKnownVideoHosts(String url) {
        assertTrue(VideoUrls.isAllowed(url), url);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://evil.com/watch?v=x",
            "https://youtube.com.evil.com/watch?v=x",
            "https://notyoutube.com/watch?v=x",
            "https://user@youtube.com/watch?v=x",
            "javascript:alert(1)",
            "www.youtube.com/watch?v=x",
            "https://rutube.ru/video/abc",
            "not a url"
    })
    void rejectsOtherSchemesAndHosts(String url) {
        assertFalse(VideoUrls.isAllowed(url), url);
    }

    @Test
    void rejectsTooLongLink() {
        String url = "https://www.youtube.com/watch?v=" + "a".repeat(VideoUrls.MAX_LENGTH);
        assertFalse(VideoUrls.isAllowed(url));
    }

    @Test
    void normalizesBlankToNull() {
        assertNull(VideoUrls.normalize("  "));
        assertNull(VideoUrls.normalize(null));
        assertEquals("https://youtu.be/x", VideoUrls.normalize(" https://youtu.be/x "));
    }

    @Test
    void parsesVkVideoAttachment() {
        assertEquals(Optional.of("video-123_456"), VideoUrls.vkVideoAttachment("https://vk.com/video-123_456"));
        assertEquals(Optional.of("video123_456"), VideoUrls.vkVideoAttachment("https://vkvideo.ru/video123_456"));
        assertEquals(Optional.of("video-123_456"),
                VideoUrls.vkVideoAttachment("https://vk.com/video_ext.php?oid=-123&id=456&hash=abc"));
        assertEquals(Optional.empty(), VideoUrls.vkVideoAttachment("https://www.youtube.com/watch?v=x"));
        assertEquals(Optional.empty(), VideoUrls.vkVideoAttachment("https://vk.com/club123"));
        assertEquals(Optional.empty(), VideoUrls.vkVideoAttachment("https://vk.com/video_ext.php?oid=x&id=1"));
        assertEquals(Optional.empty(), VideoUrls.vkVideoAttachment(null));
    }

    @Test
    void webUrlAcceptsHttpAndHttpsOnly() {
        assertTrue(VideoUrls.isWebUrl("http://example.com/a", 512));
        assertTrue(VideoUrls.isWebUrl("https://example.com/a", 512));
        assertTrue(VideoUrls.isWebUrl("", 512));
        assertTrue(VideoUrls.isWebUrl(null, 512));
        assertFalse(VideoUrls.isWebUrl("ftp://example.com/a", 512));
        assertFalse(VideoUrls.isWebUrl("javascript:alert(1)", 512));
        assertFalse(VideoUrls.isWebUrl("https://example.com/" + "a".repeat(512), 512));
    }

    @Test
    void articleRequestRejectsForeignVideoHost() {
        ArticleRequest request = new ArticleRequest();
        request.setUrl("news");
        request.setType(ArticleType.NEWS);
        request.setTitle("Новость");
        request.setPreview("[]");
        request.setContent("[]");

        request.setVideoUrl("https://www.youtube.com/watch?v=x");
        assertTrue(validator.validate(request).isEmpty());

        request.setVideoUrl("https://evil.com/video");
        Set<ConstraintViolation<ArticleRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("videoUrl", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void communityNewsRequestValidatesLinks() {
        CommunityNewsRequest request = new CommunityNewsRequest();
        request.setUrl("news");
        request.setTitle("Новость");
        request.setAuthorName("");
        request.setPreview("[\"текст\"]");
        request.setContent("");
        assertTrue(validator.validate(request).isEmpty(), "пустые автор и текст допустимы");

        request.setSourceUrl("ftp://example.com");
        request.setVideoUrl("http://youtu.be/x");
        Set<String> fields = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(Set.of("sourceUrl", "videoUrl"), fields);
    }
}
