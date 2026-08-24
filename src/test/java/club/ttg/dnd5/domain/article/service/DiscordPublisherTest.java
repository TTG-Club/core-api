package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.config.properties.DiscordProperties;
import club.ttg.dnd5.domain.article.model.Article;
import club.ttg.dnd5.domain.article.model.DiscordMention;
import club.ttg.dnd5.domain.vttg.service.VttgMarkupConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Длинный пост, который не влезает в лимит Discord: пинг должен звенеть в КАЖДОМ сообщении, а обложка —
 * ехать с последним (см. {@link DiscordPublisher}).
 */
class DiscordPublisherTest {

    private static final String WEBHOOK = "https://discord.com/api/webhooks/1/token";
    private static final String SEND_URL = WEBHOOK + "?wait=true";
    private static final String ROLE_ID = "555000111222333444";
    /** Строка пинга роли, которую ждём первой строкой каждого сообщения. */
    private static final String ROLE_PING = "<@&" + ROLE_ID + ">";
    /** Пинг звенит, только если роль явно разрешена в {@code allowed_mentions} — и в multipart тоже. */
    private static final String ALLOWED_ROLES = "\"roles\":[\"" + ROLE_ID + "\"]";
    private static final String COVER_PATH = "/s3/articles/cover.png";
    private static final String SITE_URL = "https://ttg.club";

    /** Ответ вебхука на {@code ?wait=true} — публикатору из него нужен только id сообщения. */
    private static final String OK_RESPONSE = "{\"id\":\"777\"}";

    /** Лимит длины сообщения Discord — вместе с пингом текст обязан в него укладываться. */
    private static final int MESSAGE_LIMIT = 2000;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockRestServiceServer server;
    private ArticleImageSource imageSource;
    private DiscordPublisher publisher;

    @BeforeEach
    void setUp() {
        // Mock привязываем к билдеру и только потом строим RestClient: иначе requestFactory затёрла бы
        // mock-фабрику и запросы уходили бы в реальную сеть.
        RestClient.Builder restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();

        DiscordProperties properties = new DiscordProperties();
        properties.setWebhookUrl(WEBHOOK);
        properties.setServerRoleId(ROLE_ID);

        DiscordMarkdownFormatter formatter =
                new DiscordMarkdownFormatter(new VttgMarkupConverter(objectMapper), objectMapper);
        ReflectionTestUtils.setField(formatter, "appUrl", SITE_URL);

        imageSource = mock(ArticleImageSource.class);
        publisher = new DiscordPublisher(restClientBuilder.build(), properties, formatter, imageSource, objectMapper);
    }

    @Test
    void longPostPingsEveryMessageAndAttachesCoverToLast() {
        when(imageSource.bytes(COVER_PATH)).thenReturn(new byte[]{1, 2, 3});
        when(imageSource.filename(COVER_PATH)).thenReturn("cover.png");

        // Первое сообщение: пинг + заголовок + первый абзац, текстом (обложка едет последней).
        server.expect(requestTo(SEND_URL))
                .andExpect(method(POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").value(startsWith(ROLE_PING + "\n\n**Бестиарий пополнился**")))
                .andExpect(jsonPath("$.allowed_mentions.roles[0]").value(ROLE_ID))
                .andRespond(withSuccess(OK_RESPONSE, MediaType.APPLICATION_JSON));
        // Середина хвоста: пинг есть, заголовка и обложки нет.
        server.expect(requestTo(SEND_URL))
                .andExpect(method(POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").value(startsWith(ROLE_PING + "\n\n")))
                .andExpect(jsonPath("$.content").value(not(containsString("**Бестиарий пополнился**"))))
                .andExpect(jsonPath("$.allowed_mentions.roles[0]").value(ROLE_ID))
                .andRespond(withSuccess(OK_RESPONSE, MediaType.APPLICATION_JSON));
        // Последнее сообщение: multipart с файлом обложки, пинг в payload_json тоже на месте.
        server.expect(requestTo(SEND_URL))
                .andExpect(method(POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andExpect(content().string(containsString("cover.png")))
                .andExpect(content().string(containsString(ROLE_PING)))
                .andExpect(content().string(containsString(ALLOWED_ROLES)))
                .andRespond(withSuccess(OK_RESPONSE, MediaType.APPLICATION_JSON));

        Article article = article();
        assertEquals(DiscordPublisher.PublishResult.Status.POSTED, publisher.publish(article).status());
        server.verify();
    }

    @Test
    void shortPostKeepsCoverOnItsOnlyMessage() {
        when(imageSource.bytes(COVER_PATH)).thenReturn(new byte[]{1, 2, 3});
        when(imageSource.filename(COVER_PATH)).thenReturn("cover.png");

        server.expect(requestTo(SEND_URL))
                .andExpect(method(POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andExpect(content().string(containsString("cover.png")))
                .andExpect(content().string(containsString(ROLE_PING)))
                .andExpect(content().string(containsString(ALLOWED_ROLES)))
                .andRespond(withSuccess(OK_RESPONSE, MediaType.APPLICATION_JSON));

        Article article = article();
        article.setPreview("[\"Полсотни новых существ.\"]");

        assertEquals(DiscordPublisher.PublishResult.Status.POSTED, publisher.publish(article).status());
        server.verify();
    }

    /**
     * Строка пинга занимает место в сообщении, поэтому куски текста режутся под уменьшенный лимит:
     * ни одно сообщение не должно вылезти за 2000 символов (Discord отверг бы его целиком).
     */
    @Test
    void everyMessageFitsDiscordLimitWithPing() {
        Article article = article();
        // Без обложки: все сообщения уходят JSON-ом, и содержимое каждого видно матчеру.
        article.setPreviewImageUrl(null);
        // Абзацы вокруг границы: ровно под лимит с пингом, на символ больше (пойдёт в разбивку по словам)
        // и заведомо огромный (разобьётся на несколько кусков).
        int bodyLimit = MESSAGE_LIMIT - ROLE_PING.length() - 2;
        article.setPreview(preview(
                paragraph("Полсотни новых существ. ", bodyLimit),
                paragraph("Фильтры стали точнее. ", bodyLimit + 1),
                paragraph("Обложку рисовал художник. ", 5000)));

        List<String> sent = new ArrayList<>();
        server.expect(ExpectedCount.manyTimes(), requestTo(SEND_URL))
                .andExpect(method(POST))
                .andExpect(collectingContent(sent))
                .andRespond(withSuccess(OK_RESPONSE, MediaType.APPLICATION_JSON));

        assertEquals(DiscordPublisher.PublishResult.Status.POSTED, publisher.publish(article).status());
        server.verify();

        assertTrue(sent.size() > 1, "текст должен был уехать несколькими сообщениями, а ушло: " + sent.size());
        for (String content : sent) {
            assertTrue(content.startsWith(ROLE_PING + "\n\n"),
                    "пинг не первой строкой: " + content.substring(0, Math.min(60, content.length())));
            assertEquals(1, countOccurrences(content, ROLE_PING), "пинг задвоился в сообщении");
            assertTrue(content.length() <= MESSAGE_LIMIT,
                    "сообщение длиннее лимита Discord (" + MESSAGE_LIMIT + "): " + content.length());
        }
    }

    /**
     * Складывает текст каждого сообщения в список — проверяем их после отправки, а не прямо в матчере:
     * упавший внутри матчера assert {@code MockRestServiceServer} маскирует под «No further requests
     * expected», и по такому сообщению причину не понять.
     */
    private RequestMatcher collectingContent(List<String> sink) {
        return request -> {
            String body = ((MockClientHttpRequest) request).getBodyAsString();
            sink.add(objectMapper.readTree(body).path("content").asText());
        };
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        for (int at = haystack.indexOf(needle); at >= 0; at = haystack.indexOf(needle, at + needle.length())) {
            count++;
        }
        return count;
    }

    /** Новость с пингом роли и обложкой, текст которой не влезает в одно сообщение Discord. */
    private static Article article() {
        Article article = new Article();
        article.setUrl("bestiary-update");
        article.setTitle("Бестиарий пополнился");
        article.setPreview("[\"" + paragraph("Полсотни новых существ. ")
                + "\",\"" + paragraph("Фильтры стали точнее. ")
                + "\",\"" + paragraph("Обложку рисовал художник. ") + "\"]");
        article.setPreviewImageUrl(COVER_PATH);
        article.setDiscordMention(DiscordMention.SERVER);
        return article;
    }

    /** Абзац ~1500 символов: два таких в одно сообщение (лимит 2000) уже не помещаются. */
    private static String paragraph(String sentence) {
        return paragraph(sentence, 1500);
    }

    /** Абзац ровно заданной длины — чтобы проверять поведение на границе лимита. */
    private static String paragraph(String sentence, int length) {
        return sentence.repeat(length / sentence.length() + 1).substring(0, length).strip() + ".";
    }

    /** Абзацы → разметка новости (массив блоков), как её хранит {@code article.preview}. */
    private String preview(String... paragraphs) {
        try {
            return objectMapper.writeValueAsString(List.of(paragraphs));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
