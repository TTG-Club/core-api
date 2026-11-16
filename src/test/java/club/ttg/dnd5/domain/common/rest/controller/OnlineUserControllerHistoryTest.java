package club.ttg.dnd5.domain.common.rest.controller;

import club.ttg.dnd5.domain.common.service.OnlineUserService;
import club.ttg.dnd5.exception.ExceptionController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
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

/**
 * История посещаемости для админки. Контроллер берётся из контекста с включённой проверкой
 * {@code @Secured}, как в приложении; подменён только поход в online-service.
 */
class OnlineUserControllerHistoryTest
{
    private static final String HISTORY_URL = "/api/v2/online/history";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OnlineUserService service = mock(OnlineUserService.class);

    private AnnotationConfigApplicationContext context;
    private MockMvc mockMvc;

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class SecuredControllerConfig
    {
        @Bean
        OnlineUserController onlineUserController(OnlineUserService service)
        {
            return new OnlineUserController(service);
        }
    }

    @BeforeEach
    void setUp()
    {
        context = new AnnotationConfigApplicationContext();
        context.registerBean(OnlineUserService.class, () -> service);
        context.register(SecuredControllerConfig.class);
        context.refresh();

        mockMvc = MockMvcBuilders
                .standaloneSetup(context.getBean(OnlineUserController.class))
                .setControllerAdvice(new ExceptionController())
                .build();
    }

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.clearContext();
        context.close();
    }

    @Test
    void historyIsForbiddenWithoutAdminRole() throws Exception
    {
        signInWith("USER");

        assertEquals(403, getHistory(HISTORY_URL).getStatus());
        verifyNoInteractions(service);
    }

    @Test
    void historyIsPassedToAdminAsIsWithNulls() throws Exception
    {
        signInWith("ADMIN");
        when(service.getHistory(90)).thenReturn(new OnlineUserService.OnlineHistoryResponse(
                "Europe/Moscow", "2026-08-19", "2026-11-16",
                new OnlineUserService.OnlineHistorySeries(List.of(
                        new OnlineUserService.OnlineHistoryDay("2026-11-15",
                                null, null, null, null, null, null, null),
                        new OnlineUserService.OnlineHistoryDay("2026-11-16",
                                812L, 700L, 112L, 41L, "2026-11-16T21:35:00+03:00", 17.3, 3L)
                )),
                List.of(new OnlineUserService.OnlineSiteHistory("5e14", List.of()))
        ));

        MockHttpServletResponse response = getHistory(HISTORY_URL + "?days=90");

        assertEquals(200, response.getStatus());
        assertEquals(
                objectMapper.readTree("""
                        {
                          "zone": "Europe/Moscow", "from": "2026-08-19", "to": "2026-11-16",
                          "total": { "days": [
                            { "date": "2026-11-15", "visitors": null, "guests": null, "registered": null,
                              "peakOnline": null, "peakAt": null, "averageOnline": null, "peakPlayers": null },
                            { "date": "2026-11-16", "visitors": 812, "guests": 700, "registered": 112,
                              "peakOnline": 41, "peakAt": "2026-11-16T21:35:00+03:00",
                              "averageOnline": 17.3, "peakPlayers": 3 }
                          ] },
                          "sites": [ { "siteId": "5e14", "days": [] } ]
                        }
                        """),
                objectMapper.readTree(response.getContentAsString(StandardCharsets.UTF_8))
        );
    }

    @Test
    void historyDaysAreClampedAndDefaultTo30() throws Exception
    {
        signInWith("ADMIN");

        getHistory(HISTORY_URL);
        getHistory(HISTORY_URL + "?days=0");
        getHistory(HISTORY_URL + "?days=1000");

        verify(service).getHistory(30);
        verify(service).getHistory(1);
        verify(service).getHistory(365);
    }

    private static void signInWith(String role)
    {
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                "user", null, AuthorityUtils.createAuthorityList(role)));
    }

    private MockHttpServletResponse getHistory(String url) throws Exception
    {
        return mockMvc.perform(get(url)).andReturn().getResponse();
    }
}
