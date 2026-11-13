package club.ttg.dnd5.domain.tool.dmscreen.rest.controller;

import club.ttg.dnd5.domain.tool.dmscreen.model.DmScreen;
import club.ttg.dnd5.domain.tool.dmscreen.model.DmScreenWindow;
import club.ttg.dnd5.domain.tool.dmscreen.repository.DmScreenRepository;
import club.ttg.dnd5.domain.tool.dmscreen.service.DmScreenService;
import club.ttg.dnd5.domain.user.model.Role;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ExceptionController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Контракт ручек ширмы Мастера, под который написан сайт: коды ответов, имена полей в JSON и
 * проверки окон. Запросы идут через настоящие контроллер, сервис и обработчик ошибок; подменено
 * только хранилище — оно, как jsonb-колонка, держит окна JSON-текстом, а не объектами.
 */
class DmScreenControllerTest {

    private static final String URL = "/api/v2/tools/dm-screen";
    private static final String NOTE_TEXT = "КД > 15 <важно>";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<UUID, String> storedWindows = new HashMap<>();
    private final Map<UUID, Instant> storedAt = new HashMap<>();
    private final DmScreenRepository repository = mock(DmScreenRepository.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new DmScreenController(new DmScreenService(repository)))
            .setControllerAdvice(new ExceptionController())
            // Как в приложении: Spring Boot пишет даты строкой ISO, а не числом.
            .setMessageConverters(new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json()
                    .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .build()))
            .build();

    @BeforeEach
    void storeScreensAsJson() {
        when(repository.findByUserId(any())).thenAnswer(invocation -> {
            UUID userId = invocation.getArgument(0);
            if (!storedWindows.containsKey(userId)) {
                return Optional.empty();
            }
            DmScreen screen = new DmScreen();
            screen.setId(UUID.randomUUID());
            screen.setUserId(userId);
            screen.setWindows(objectMapper.readValue(storedWindows.get(userId),
                    new TypeReference<List<DmScreenWindow>>() { }));
            screen.setUpdatedAt(storedAt.get(userId));
            return Optional.of(screen);
        });
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            DmScreen screen = invocation.getArgument(0);
            storedWindows.put(screen.getUserId(), objectMapper.writeValueAsString(screen.getWindows()));
            storedAt.put(screen.getUserId(), Instant.now());
            return screen;
        });
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void withoutTokenBothEndpointsAnswer401() throws Exception {
        assertEquals(401, mockMvc.perform(get(URL)).andReturn().getResponse().getStatus());
        assertEquals(401, save(windows(pageWindow())).getResponse().getStatus());

        // Так аноним выглядит в приложении: фильтр Spring Security кладёт в контекст анонимный токен.
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        assertEquals(401, mockMvc.perform(get(URL)).andReturn().getResponse().getStatus());
        MvcResult result = save(windows(pageWindow()));
        assertEquals(401, result.getResponse().getStatus());
        assertEquals("Пользователь не авторизован", body(result).get("message").asText());
        assertTrue(storedWindows.isEmpty());
    }

    @Test
    void tokenWithoutUuidIsNotAuthorized() throws Exception {
        authenticate(null);

        assertEquals(401, mockMvc.perform(get(URL)).andReturn().getResponse().getStatus());
        assertEquals(401, save(windows(pageWindow())).getResponse().getStatus());
    }

    @Test
    void newUserGetsEmptyScreenInsteadOf404() throws Exception {
        authenticate(UUID.randomUUID());

        MvcResult result = mockMvc.perform(get(URL)).andReturn();

        assertEquals(200, result.getResponse().getStatus());
        JsonNode json = body(result);
        assertTrue(json.get("windows").isArray());
        assertEquals(0, json.get("windows").size());
        assertTrue(json.has("updatedAt"));
        assertTrue(json.get("updatedAt").isNull());
    }

    @Test
    void savedWindowsComeBackUnchangedAndInOrder() throws Exception {
        authenticate(UUID.randomUUID());
        ObjectNode page = pageWindow();
        page.put("x", -12.5).put("y", 8.25);
        ObjectNode note = noteWindow("[\"Засада у моста: 4 гоблина\"]");
        note.put("isMinimized", true);
        ArrayNode windows = windows(note, page);

        MvcResult saved = save(windows);

        assertEquals(204, saved.getResponse().getStatus());
        assertEquals("", saved.getResponse().getContentAsString());

        MvcResult loaded = mockMvc.perform(get(URL)).andReturn();
        assertEquals(200, loaded.getResponse().getStatus());
        JsonNode json = body(loaded);
        assertEquals(windows, json.get("windows"));
        assertTrue(json.get("windows").get(0).get("isMinimized").asBoolean());
        assertFalse(json.get("windows").get(1).get("isMinimized").asBoolean());
        assertFalse(json.get("windows").get(0).has("minimized"));
        assertTrue(json.get("windows").get(1).get("content").isNull());
        assertNotNull(Instant.parse(json.get("updatedAt").asText()));
    }

    @Test
    void savingReplacesWindowsCompletely() throws Exception {
        authenticate(UUID.randomUUID());
        save(windows(pageWindow(), noteWindow("[\"первая\"]")));

        assertEquals(204, save(windows()).getResponse().getStatus());

        assertEquals(0, body(mockMvc.perform(get(URL)).andReturn()).get("windows").size());
    }

    @Test
    void foreignScreenIsInvisible() throws Exception {
        authenticate(UUID.randomUUID());
        save(windows(noteWindow("[\"секрет мастера\"]")));

        authenticate(UUID.randomUUID());
        JsonNode stranger = body(mockMvc.perform(get(URL)).andReturn());
        assertEquals(0, stranger.get("windows").size());
        assertTrue(stranger.get("updatedAt").isNull());

        // И запись второго пользователя не трогает ширму первого.
        save(windows(pageWindow()));
        assertEquals(2, storedWindows.size());
    }

    @Test
    void noteWithAngleBracketsIsStoredAsIs() throws Exception {
        authenticate(UUID.randomUUID());

        assertEquals(204, save(windows(noteWindow(NOTE_TEXT))).getResponse().getStatus());

        JsonNode window = body(mockMvc.perform(get(URL)).andReturn()).get("windows").get(0);
        assertEquals(NOTE_TEXT, window.get("content").asText());
    }

    @Test
    void limitsAreAcceptedAtTheirEdge() throws Exception {
        authenticate(UUID.randomUUID());
        ArrayNode windows = windows();
        for (int index = 0; index < 100; index++) {
            windows.add(pageWindow());
        }
        ObjectNode note = noteWindow("я".repeat(20000));
        note.put("id", "i".repeat(64)).put("title", "");
        note.put("path", "note:" + "p".repeat(251));
        windows.set(0, note);

        assertEquals(204, save(windows).getResponse().getStatus());
    }

    @Test
    void moreThanHundredWindowsIsRejected() throws Exception {
        authenticate(UUID.randomUUID());
        ArrayNode windows = windows();
        for (int index = 0; index < 101; index++) {
            windows.add(pageWindow());
        }

        assertRejected(save(windows), "На ширме может быть не больше 100 окон");
    }

    @Test
    void invalidWindowsAreRejectedWithRussianMessage() throws Exception {
        authenticate(UUID.randomUUID());

        assertRejected(window -> window.put("path", "spells/fireball-phb"),
                "Окно 1: адрес должен начинаться с «/» или с «note:»");
        assertRejected(window -> window.put("path", "https://example.com"),
                "Окно 1: адрес должен начинаться с «/» или с «note:»");
        assertRejected(window -> window.put("path", "/" + "p".repeat(256)),
                "Окно 1: адрес записи длиннее 256 символов");
        assertRejected(window -> window.remove("path"), "Окно 1: не указан адрес записи");
        assertRejected(window -> window.put("content", "[\"текст\"]"),
                "Окно 1: текст бывает только у заметок");
        assertRejected(window -> window.put("id", ""), "Окно 1: не указан идентификатор");
        assertRejected(window -> window.put("id", "   "), "Окно 1: не указан идентификатор");
        assertRejected(window -> window.remove("id"), "Окно 1: не указан идентификатор");
        assertRejected(window -> window.put("id", "i".repeat(65)),
                "Окно 1: идентификатор длиннее 64 символов");
        assertRejected(window -> window.remove("title"), "Окно 1: не указан заголовок");
        assertRejected(window -> window.put("title", "т".repeat(257)),
                "Окно 1: заголовок длиннее 256 символов");
        assertRejected(window -> window.put("width", 0),
                "Окно 1: ширина и высота должны быть больше нуля");
        assertRejected(window -> window.put("width", -480),
                "Окно 1: ширина и высота должны быть больше нуля");
        assertRejected(window -> window.put("height", 0),
                "Окно 1: ширина и высота должны быть больше нуля");
        assertRejected(window -> window.remove("height"), "Окно 1: не указан размер");
        assertRejected(window -> window.remove("x"), "Окно 1: не указано положение");
        assertRejected(window -> window.remove("layer"), "Окно 1: не указан слой");
        assertRejected(window -> window.remove("isMinimized"), "Окно 1: не указано, свёрнуто ли окно");

        assertTrue(storedWindows.isEmpty());
    }

    @Test
    void tooLongNoteIsRejectedAndNamesTheWindow() throws Exception {
        authenticate(UUID.randomUUID());

        assertRejected(save(windows(pageWindow(), noteWindow("я".repeat(20001)))),
                "Окно 2: текст заметки длиннее 20000 символов");
    }

    @Test
    void brokenBodyIsRejectedInsteadOf500() throws Exception {
        authenticate(UUID.randomUUID());

        assertRejected(saveRaw("{}"), "Не передан список окон ширмы");
        assertRejected(saveRaw("{\"windows\":null}"), "Не передан список окон ширмы");
        assertRejected(saveRaw("{\"windows\":[null]}"), "Окно 1: окно не заполнено");
        assertRejected(saveRaw("{\"windows\":[{\"width\":\"широкое\"}]}"), "Не удалось прочитать окна ширмы");
        assertRejected(saveRaw("не json"), "Не удалось прочитать окна ширмы");
    }

    @Test
    void simultaneousFirstSaveWritesIntoTheCreatedScreen() throws Exception {
        UUID userId = authenticate(UUID.randomUUID());
        // Параллельный запрос успел создать запись между поиском и вставкой — упираемся в уникальность.
        doAnswer(invocation -> {
            storedWindows.put(userId, "[]");
            storedAt.put(userId, Instant.now());
            throw new DataIntegrityViolationException("dm_screen_user_id_index");
        }).doAnswer(invocation -> {
            DmScreen screen = invocation.getArgument(0);
            assertNotNull(screen.getId());
            storedWindows.put(userId, objectMapper.writeValueAsString(screen.getWindows()));
            return screen;
        }).when(repository).saveAndFlush(any());

        assertEquals(204, save(windows(noteWindow(NOTE_TEXT))).getResponse().getStatus());

        verify(repository, times(2)).saveAndFlush(any());
        assertEquals(NOTE_TEXT, objectMapper.readTree(storedWindows.get(userId)).get(0).get("content").asText());
    }

    private void assertRejected(Consumer<ObjectNode> breakWindow, String message) throws Exception {
        ObjectNode window = pageWindow();
        breakWindow.accept(window);
        assertRejected(save(windows(window)), message);
    }

    private void assertRejected(MvcResult result, String message) throws Exception {
        assertEquals(400, result.getResponse().getStatus(), message);
        assertEquals(message, body(result).get("message").asText());
    }

    private MvcResult save(ArrayNode windows) throws Exception {
        ObjectNode request = objectMapper.createObjectNode();
        request.set("windows", windows);
        return saveRaw(objectMapper.writeValueAsString(request));
    }

    private MvcResult saveRaw(String json) throws Exception {
        return mockMvc.perform(put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.getBytes(StandardCharsets.UTF_8)))
                .andReturn();
    }

    private JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private ArrayNode windows(ObjectNode... windows) {
        ArrayNode array = objectMapper.createArrayNode();
        for (ObjectNode window : windows) {
            array.add(window);
        }
        return array;
    }

    private ObjectNode pageWindow() {
        ObjectNode window = objectMapper.createObjectNode();
        window.put("id", UUID.randomUUID().toString());
        window.put("path", "/spells/fireball-phb");
        window.put("title", "Огненный шар");
        window.put("x", 8.0);
        window.put("y", 8.0);
        window.put("width", 480.0);
        window.put("height", 600.0);
        window.put("layer", 3);
        window.put("isMinimized", false);
        window.putNull("content");
        return window;
    }

    private ObjectNode noteWindow(String content) {
        String id = UUID.randomUUID().toString();
        ObjectNode window = pageWindow();
        window.put("id", id);
        window.put("path", "note:" + id);
        window.put("title", "Заметка");
        window.put("layer", 4);
        window.put("content", content);
        return window;
    }

    private static UUID authenticate(UUID userId) {
        User user = new User();
        user.setUuid(userId);
        user.setUsername("master");
        user.setRoles(List.of(Role.builder().name("USER").build()));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        return userId;
    }
}
