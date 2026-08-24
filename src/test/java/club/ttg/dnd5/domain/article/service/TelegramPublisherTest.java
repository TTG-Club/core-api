package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.config.properties.TelegramProperties;
import club.ttg.dnd5.domain.article.model.Article;
import club.ttg.dnd5.domain.article.model.TelegramPostFormat;
import club.ttg.dnd5.domain.vttg.service.VttgMarkupConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Пост с карточкой Instant View: что уходит в {@code text} сообщения. По умолчанию там невидимая
 * заглушка — под карточкой не должно быть ни одной лишней строки; с включённой в админке галочкой
 * «короткое описание» вместо неё идёт выжимка новости (см. {@code docs/telegram-instant-view.md}).
 */
class TelegramPublisherTest {

    private static final String API_URL = "https://api.telegram.org";
    private static final String BOT_TOKEN = "123456:AA-test";
    private static final String CHAT_ID = "@ttg_club";
    private static final String RHASH = "abcdef123456";
    private static final String SITE_URL = "https://ttg.club";

    /** Невидимая заглушка (U+2060 WORD JOINER) — текст поста, когда короткого описания нет. */
    private static final String BLANK_TEXT = "\u2060";

    private static final String SEND_MESSAGE = API_URL + "/bot" + BOT_TOKEN + "/sendMessage";
    private static final String EDIT_MESSAGE = API_URL + "/bot" + BOT_TOKEN + "/editMessageText";

    /** Ответ Bot API на успешную отправку — публикатору из него нужен только {@code message_id}. */
    private static final String OK_RESPONSE = "{\"ok\":true,\"result\":{\"message_id\":42}}";

    private MockRestServiceServer server;
    private TelegramPublisher publisher;

    @BeforeEach
    void setUp() {
        // Mock привязываем к билдеру и только потом строим RestClient: иначе baseUrl/requestFactory
        // затёрли бы mock-фабрику и запросы уходили бы в реальную сеть.
        RestClient.Builder restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.baseUrl(API_URL).build();

        TelegramProperties properties = new TelegramProperties();
        properties.setBotToken(BOT_TOKEN);
        properties.setChatId(CHAT_ID);
        properties.setInstantViewRhash(RHASH);

        ObjectMapper objectMapper = new ObjectMapper();
        TelegramHtmlFormatter formatter =
                new TelegramHtmlFormatter(new VttgMarkupConverter(objectMapper), objectMapper);

        publisher = new TelegramPublisher(restClient, properties, formatter, new ArticleImageSource(null));

        // app.url в юнит-тесте не поднимается из конфига, а без публичной базы режим Instant View
        // выключается (робот Telegram до localhost не дойдёт) — задаём боевой адрес вручную.
        ReflectionTestUtils.setField(publisher, "appUrl", SITE_URL);
        ReflectionTestUtils.setField(formatter, "appUrl", SITE_URL);
    }

    @Test
    void instantViewPostCarriesSummaryWhenEnabled() {
        Article article = article();
        article.setTelegramSummaryEnabled(true);
        article.setTelegramSummary("Главное: **полсотни существ** и новые фильтры.");

        expectSendMessage("Главное: <b>полсотни существ</b> и новые фильтры.");

        assertEquals(TelegramPublisher.PublishResult.Status.POSTED, publisher.publish(article).status());
        server.verify();
    }

    @Test
    void instantViewPostStaysBlankWithoutSummary() {
        expectSendMessage(BLANK_TEXT);

        assertEquals(TelegramPublisher.PublishResult.Status.POSTED, publisher.publish(article()).status());
        server.verify();
    }

    @Test
    void blankSummaryTextCountsAsDisabled() {
        Article article = article();
        article.setTelegramSummaryEnabled(true);
        article.setTelegramSummary("   ");

        expectSendMessage(BLANK_TEXT);

        assertEquals(TelegramPublisher.PublishResult.Status.POSTED, publisher.publish(article).status());
        server.verify();
    }

    @Test
    void editSyncsChangedSummary() {
        Article article = article();
        article.setTelegramMessageId(42L);
        article.setTelegramSummaryEnabled(true);
        article.setTelegramSummary("Дополнили: {@i ещё и фильтры}.");

        server.expect(requestTo(EDIT_MESSAGE))
                .andExpect(method(POST))
                .andExpect(jsonPath("$.message_id").value(42))
                .andExpect(jsonPath("$.text").value("Дополнили: <i>ещё и фильтры</i>."))
                .andRespond(withSuccess(OK_RESPONSE, MediaType.APPLICATION_JSON));

        assertEquals(TelegramPublisher.EditResult.SYNCED, publisher.editPost(article));
        server.verify();
    }

    /** Ждём один {@code sendMessage} с заданным текстом и карточкой Instant View в превью ссылки. */
    private void expectSendMessage(String text) {
        server.expect(requestTo(SEND_MESSAGE))
                .andExpect(method(POST))
                .andExpect(jsonPath("$.chat_id").value(CHAT_ID))
                .andExpect(jsonPath("$.text").value(text))
                .andExpect(jsonPath("$.link_preview_options.url")
                        .value("https://t.me/iv?url=https%3A%2F%2Fttg.club%2Fiv%2Farticles%2Fbestiary-update&rhash="
                                + RHASH))
                .andRespond(withSuccess(OK_RESPONSE, MediaType.APPLICATION_JSON));
    }

    /** Новость, у которой выбран компактный вид поста (карточка Instant View). */
    private static Article article() {
        Article article = new Article();
        article.setUrl("bestiary-update");
        article.setTitle("Бестиарий пополнился");
        article.setPreview("[\"Полсотни новых существ.\"]");
        article.setTelegramFormat(TelegramPostFormat.INSTANT_VIEW);
        return article;
    }
}
