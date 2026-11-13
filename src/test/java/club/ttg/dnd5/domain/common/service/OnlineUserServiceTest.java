package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.config.properties.OnlineServiceProperties;
import club.ttg.dnd5.domain.common.model.OnlineType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Посредник к online-service. Сервис сам собирает HTTP-клиент со своими таймаутами, поэтому
 * вместо подмены клиента поднимаем на время теста настоящий локальный HTTP-сервер и смотрим,
 * что до него дошло.
 */
class OnlineUserServiceTest
{
    private static final String TOKEN = "online-secret";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<String> receivedMethod = new AtomicReference<>();
    private final AtomicReference<String> receivedToken = new AtomicReference<>();
    private final AtomicReference<String> receivedBody = new AtomicReference<>();

    private HttpServer server;
    private OnlineUserService service;

    @BeforeEach
    void startOnlineService() throws IOException
    {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.start();

        OnlineServiceProperties properties = new OnlineServiceProperties();
        properties.setUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.setApiToken(TOKEN);

        service = new OnlineUserService(properties, RestClient.builder());
    }

    @AfterEach
    void stopOnlineService()
    {
        server.stop(0);
    }

    @Test
    void usageIsForwardedWithSiteIdAndToken() throws IOException
    {
        respond("/api/v1/online/usage", 204, null);

        service.usage("dm-screen", "guest-uuid", OnlineType.GUEST, 60);

        assertEquals("POST", receivedMethod.get());
        assertEquals(TOKEN, receivedToken.get());

        JsonNode body = objectMapper.readTree(receivedBody.get());
        assertEquals(5, body.size());
        assertEquals("5e24", body.get("siteId").asText());
        assertEquals("dm-screen", body.get("feature").asText());
        assertEquals("guest-uuid", body.get("key").asText());
        assertEquals("GUEST", body.get("type").asText());
        assertEquals(60, body.get("activeSeconds").asInt());
    }

    @Test
    void usageKeepsClientErrorStatusOfOnlineService()
    {
        respond("/api/v1/online/usage", 400, null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.usage("dm-screen", "guest-uuid", OnlineType.GUEST, 60));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void usageTurnsOnlineServiceFailureIntoBadGateway()
    {
        respond("/api/v1/online/usage", 500, null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.usage("dm-screen", "guest-uuid", OnlineType.GUEST, 60));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }

    @Test
    void statsCarryUsageFromOnlineService()
    {
        respond("/api/v1/online/stats", 200, """
                {
                  "windowMinutes": 30,
                  "total": { "guests": 5, "registered": 2, "players": 1, "total": 7 },
                  "sites": [
                    {
                      "windowMinutes": 30, "siteId": "5e24",
                      "guests": 5, "registered": 2, "players": 1, "total": 7,
                      "preferences": [],
                      "usage": [
                        {
                          "feature": "dm-screen",
                          "periods": [
                            { "days": 1, "visitors": 40, "users": 18, "returning": 0,
                              "activeDays": 18, "activeMinutes": 1340 }
                          ]
                        }
                      ]
                    }
                  ]
                }
                """);

        OnlineUserService.OnlineAdminStatsResponse stats = service.getStats();

        assertEquals(TOKEN, receivedToken.get());
        assertEquals(
                List.of(new OnlineUserService.FeatureUsageStats(
                        "dm-screen",
                        List.of(new OnlineUserService.UsagePeriodStats(1, 40, 18, 0, 18, 1340))
                )),
                stats.sites().getFirst().usage()
        );
    }

    @Test
    void statsWorkWhileOnlineServiceDoesNotSendUsageYet()
    {
        respond("/api/v1/online/stats", 200, """
                {
                  "windowMinutes": 30,
                  "total": { "guests": 5, "registered": 2, "players": 1, "total": 7 },
                  "sites": [
                    { "windowMinutes": 30, "siteId": "5e24",
                      "guests": 5, "registered": 2, "players": 1, "total": 7, "preferences": [] }
                  ]
                }
                """);

        OnlineUserService.OnlineStatsResponse site = service.getStats().sites().getFirst();

        assertEquals(7, site.total());
        assertNull(site.usage());
    }

    private void respond(String path, int status, String json)
    {
        server.createContext(path, exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            receivedToken.set(exchange.getRequestHeaders().getFirst("X-Online-Token"));
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

            if (json == null)
            {
                exchange.sendResponseHeaders(status, -1);
            }
            else
            {
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(status, bytes.length);
                exchange.getResponseBody().write(bytes);
            }

            exchange.close();
        });
    }
}
