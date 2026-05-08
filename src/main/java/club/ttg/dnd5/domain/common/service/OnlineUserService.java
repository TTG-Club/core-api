package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.config.properties.OnlineServiceProperties;
import club.ttg.dnd5.domain.common.model.OnlineType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Service
public class OnlineUserService
{
    private static final String ONLINE_TOKEN_HEADER = "X-Online-Token";

    private final OnlineServiceProperties properties;
    private final RestClient restClient;

    public OnlineUserService(OnlineServiceProperties properties, RestClient.Builder restClientBuilder)
    {
        if (properties.getUrl() == null || properties.getUrl().isBlank())
        {
            throw new IllegalStateException("online.service.url is not set");
        }

        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.getUrl())
                .requestFactory(requestFactory(properties))
                .build();
    }

    public HeartbeatResponse heartbeat(OnlineType type, String key, String previousGuestKey)
    {
        try
        {
            HeartbeatResponse response = restClient.post()
                    .uri("/api/v1/online/heartbeat")
                    .headers(this::addServiceHeaders)
                    .body(new HeartbeatRequest(properties.getSiteId(), key, previousGuestKey, type))
                    .retrieve()
                    .body(HeartbeatResponse.class);

            return response == null ? new HeartbeatResponse(0) : response;
        }
        catch (RestClientResponseException ex)
        {
            throw asResponseStatusException(ex);
        }
        catch (RestClientException ex)
        {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Online service is unavailable", ex);
        }
    }

    public OnlineCount getCount(Duration window)
    {
        try
        {
            OnlineStatsResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/online/stats/{siteId}")
                            .queryParam("windowMinutes", window.toMinutes())
                            .build(properties.getSiteId()))
                    .headers(this::addServiceHeaders)
                    .retrieve()
                    .body(OnlineStatsResponse.class);

            if (response == null)
            {
                return new OnlineCount(0, 0, 0);
            }

            return new OnlineCount(response.guests(), response.registered(), response.total());
        }
        catch (RestClientResponseException ex)
        {
            throw asResponseStatusException(ex);
        }
        catch (RestClientException ex)
        {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Online service is unavailable", ex);
        }
    }

    private void addServiceHeaders(HttpHeaders headers)
    {
        String token = properties.getApiToken();

        if (token != null && !token.isBlank())
        {
            headers.set(ONLINE_TOKEN_HEADER, token);
        }
    }

    private static SimpleClientHttpRequestFactory requestFactory(OnlineServiceProperties properties)
    {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        return factory;
    }

    private static ResponseStatusException asResponseStatusException(RestClientResponseException ex)
    {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());

        if (status == null || status.is5xxServerError())
        {
            status = HttpStatus.BAD_GATEWAY;
        }

        return new ResponseStatusException(status, "Online service request failed", ex);
    }

    public record HeartbeatRequest(String siteId, String key, String previousGuestKey, OnlineType type) {}

    public record HeartbeatResponse(long total) {}

    public record OnlineStatsResponse(long windowMinutes, String siteId, long guests, long registered, long total) {}

    public record OnlineCount(long guests, long registered, long total) {}
}

