package club.ttg.dnd5.domain.common.rest.controller;

import club.ttg.dnd5.domain.common.model.OnlineType;
import club.ttg.dnd5.domain.common.service.OnlineUserService;
import club.ttg.dnd5.exception.ExceptionController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Ручки онлайна, под которые написан сайт: отметка работы с инструментом и статистика для админки.
 * Запросы идут через настоящий контроллер и обработчик ошибок; подменён только поход в online-service.
 */
class OnlineUserControllerTest
{
    private static final String USAGE_URL = "/api/v2/online/usage";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OnlineUserService service = mock(OnlineUserService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new OnlineUserController(service))
            .setControllerAdvice(new ExceptionController())
            .build();

    @Test
    void usageIsPassedToOnlineServiceAndAnswers204() throws Exception
    {
        MockHttpServletResponse response = postUsage("""
                { "feature": "dm-screen", "key": "guest-uuid", "type": "GUEST", "activeSeconds": 60 }
                """);

        assertEquals(204, response.getStatus());
        assertEquals("", response.getContentAsString());
        verify(service).usage("dm-screen", "guest-uuid", OnlineType.GUEST, 60);
    }

    @Test
    void usageAcceptsBoundaryActiveSeconds() throws Exception
    {
        assertEquals(204, postUsage("""
                { "feature": "dm-screen", "key": "player", "type": "REGISTERED", "activeSeconds": 0 }
                """).getStatus());
        assertEquals(204, postUsage("""
                { "feature": "dm-screen", "key": "player", "type": "REGISTERED", "activeSeconds": 300 }
                """).getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // нет feature
            "{ \"key\": \"guest-uuid\", \"type\": \"GUEST\", \"activeSeconds\": 60 }",
            // пустой feature
            "{ \"feature\": \" \", \"key\": \"guest-uuid\", \"type\": \"GUEST\", \"activeSeconds\": 60 }",
            // feature длиннее 32 символов
            "{ \"feature\": \"123456789012345678901234567890123\", \"key\": \"guest-uuid\", \"type\": \"GUEST\","
                    + " \"activeSeconds\": 60 }",
            // нет key
            "{ \"feature\": \"dm-screen\", \"type\": \"GUEST\", \"activeSeconds\": 60 }",
            // нет type
            "{ \"feature\": \"dm-screen\", \"key\": \"guest-uuid\", \"activeSeconds\": 60 }",
            // нет activeSeconds
            "{ \"feature\": \"dm-screen\", \"key\": \"guest-uuid\", \"type\": \"GUEST\" }",
            // отрицательное время
            "{ \"feature\": \"dm-screen\", \"key\": \"guest-uuid\", \"type\": \"GUEST\", \"activeSeconds\": -1 }",
            // больше пяти минут за одну отметку
            "{ \"feature\": \"dm-screen\", \"key\": \"guest-uuid\", \"type\": \"GUEST\", \"activeSeconds\": 301 }"
    })
    void invalidUsageAnswers400AndIsNotForwarded(String body) throws Exception
    {
        assertEquals(400, postUsage(body).getStatus());
        verifyNoInteractions(service);
    }

    @Test
    void invalidKeyLengthAnswers400() throws Exception
    {
        String body = objectMapper.writeValueAsString(new OnlineUserController.OnlineUsageRequest(
                "dm-screen", "k".repeat(129), OnlineType.GUEST, 60));

        assertEquals(400, postUsage(body).getStatus());
        verifyNoInteractions(service);
    }

    @Test
    void statsCarryUsageToAdminPanel() throws Exception
    {
        when(service.getStats()).thenReturn(new OnlineUserService.OnlineAdminStatsResponse(
                30,
                new OnlineUserService.OnlineCount(5, 2, 1, 7),
                List.of(new OnlineUserService.OnlineStatsResponse(
                        30, "5e24", 5, 2, 1, 7, List.of(),
                        List.of(new OnlineUserService.FeatureUsageStats(
                                "dm-screen",
                                List.of(new OnlineUserService.UsagePeriodStats(1, 40, 18, 0, 18, 1340))
                        ))
                ))
        ));

        MockHttpServletResponse response = mockMvc.perform(get("/api/v2/online/stats")).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        JsonNode usage = objectMapper.readTree(response.getContentAsString(StandardCharsets.UTF_8))
                .get("sites").get(0).get("usage");
        assertEquals(
                objectMapper.readTree("""
                        [ { "feature": "dm-screen", "periods": [
                          { "days": 1, "visitors": 40, "users": 18, "returning": 0,
                            "activeDays": 18, "activeMinutes": 1340 } ] } ]
                        """),
                usage
        );
    }

    private MockHttpServletResponse postUsage(String body) throws Exception
    {
        return mockMvc.perform(post(USAGE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse();
    }
}
