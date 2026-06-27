package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.config.properties.InternalServiceProperties;
import club.ttg.dnd5.domain.user.model.Role;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.security.InternalServiceTokenFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class VttgAccessServiceTest {
    private static final String BASE_URL = "http://subscriber.test";
    private static final String SECRET = "shared-secret";

    private MockRestServiceServer server;
    private VttgAccessService service;

    @BeforeEach
    void setUp() {
        // Mock привязываем к билдеру и только потом строим RestClient: иначе baseUrl/requestFactory
        // затёрли бы mock-фабрику и запросы уходили бы в реальную сеть.
        RestClient.Builder restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.baseUrl(BASE_URL).build();

        InternalServiceProperties internalProperties = new InternalServiceProperties();
        internalProperties.setServiceSecret(SECRET);

        service = new VttgAccessService(internalProperties, restClient);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminGetsFullExportWithoutCallingSubscriber() {
        authenticate("admin", "ADMIN");
        // никакого ожидания на сервере — admin не должен звать subscriber-service
        assertFalse(service.access().srdOnly());
        server.verify();
    }

    @Test
    void earlyAccessVttgRoleGetsOnlySrdWithoutActiveSubscription() {
        authenticate("early", "VTTG");
        expectStatus("early", "{\"active\":false,\"registered\":false}");

        assertTrue(service.access().srdOnly());
        server.verify();
    }

    @Test
    void activeSubscriptionGetsFullExport() {
        authenticate("subscriber", "USER");
        expectStatus("subscriber", "{\"active\":true,\"registered\":true}");

        assertFalse(service.access().srdOnly());
        server.verify();
    }

    @Test
    void registeredInactiveSubscriptionGetsOnlySrd() {
        authenticate("registered", "USER");
        expectStatus("registered", "{\"active\":false,\"registered\":true}");

        assertTrue(service.access().srdOnly());
        server.verify();
    }

    @Test
    void userWithoutSubscriptionIsRejected() {
        authenticate("user", "USER");
        expectStatus("user", "{\"active\":false,\"registered\":false}");

        assertThrows(ApiException.class, service::access);
        server.verify();
    }

    @Test
    void subscriberServiceFailureIsFailClosedForNonRegisteredUser() {
        authenticate("user", "USER");
        server.expect(requestTo(BASE_URL + "/api/internal/subscriptions/user/status"))
                .andExpect(method(GET))
                .andRespond(withServerError());

        // недоступность subscriber-service → active=false, registered=false → 403
        assertThrows(ApiException.class, service::access);
        server.verify();
    }

    private void expectStatus(String username, String body) {
        server.expect(requestTo(BASE_URL + "/api/internal/subscriptions/" + username + "/status"))
                .andExpect(method(GET))
                .andExpect(header(InternalServiceTokenFilter.SERVICE_TOKEN_HEADER, SECRET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }

    private void authenticate(String username, String role) {
        User user = new User();
        user.setUsername(username);
        user.setRoles(List.of(Role.builder().name(role).build()));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }
}
