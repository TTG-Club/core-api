package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.config.properties.OnlineServiceProperties;
import club.ttg.dnd5.domain.common.model.OnlineType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

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

    public HeartbeatResponse heartbeat(
            OnlineType type,
            String key,
            String previousGuestKey,
            Map<String, String> preferences
    )
    {
        try
        {
            HeartbeatResponse response = restClient.post()
                    .uri("/api/v1/online/heartbeat")
                    .headers(this::addServiceHeaders)
                    .body(new HeartbeatRequest(properties.getSiteId(), key, previousGuestKey, type, preferences))
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

    /**
     * Отметка о работе посетителя с инструментом сайта (например, ширмой Мастера). Считает всё
     * online-service, мы только добавляем сайт и служебный токен.
     */
    public void usage(String feature, String key, OnlineType type, int activeSeconds)
    {
        try
        {
            restClient.post()
                    .uri("/api/v1/online/usage")
                    .headers(this::addServiceHeaders)
                    .body(new UsageRequest(properties.getSiteId(), feature, key, type, activeSeconds))
                    .retrieve()
                    .toBodilessEntity();
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
                return new OnlineCount(0, 0, 0, 0);
            }

            return new OnlineCount(response.guests(), response.registered(), response.players(), response.total());
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

    public OnlineAdminStatsResponse getStats()
    {
        try
        {
            OnlineAdminStatsResponse response = restClient.get()
                    .uri("/api/v1/online/stats")
                    .headers(this::addServiceHeaders)
                    .retrieve()
                    .body(OnlineAdminStatsResponse.class);

            return response == null
                    ? new OnlineAdminStatsResponse(0, new OnlineCount(0, 0, 0, 0), List.of())
                    : response;
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

    /**
     * Посещаемость по суткам. Считает и хранит её online-service, ответ отдаём как есть.
     */
    public OnlineHistoryResponse getHistory(int days)
    {
        try
        {
            OnlineHistoryResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/online/history")
                            .queryParam("days", days)
                            .build())
                    .headers(this::addServiceHeaders)
                    .retrieve()
                    .body(OnlineHistoryResponse.class);

            return response == null
                    ? new OnlineHistoryResponse(null, null, null, new OnlineHistorySeries(List.of()), List.of())
                    : response;
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

    public record HeartbeatRequest(
            String siteId,
            String key,
            String previousGuestKey,
            OnlineType type,
            Map<String, String> preferences
    ) {}

    public record HeartbeatResponse(long total) {}

    public record UsageRequest(String siteId, String feature, String key, OnlineType type, int activeSeconds) {}

    /**
     * Поля {@code players}, {@code preferences} и {@code usage} отдаёт только online-service, и мы
     * обязаны пронести их дальше: иначе до админки они не дойдут.
     */
    @Schema(description = "Статистика одного сайта")
    public record OnlineStatsResponse(
            @Schema(description = "За сколько последних минут посчитан онлайн")
            long windowMinutes,

            @Schema(description = "Сайт", example = "5e24")
            String siteId,

            @Schema(description = "Сколько гостей онлайн")
            long guests,

            @Schema(description = "Сколько зарегистрированных пользователей онлайн")
            long registered,

            @Schema(description = "Сколько посетителей сейчас в игровых мирах. Это часть гостей и "
                    + "зарегистрированных, в total они второй раз не складываются")
            long players,

            @Schema(description = "Всего онлайн: гости и зарегистрированные")
            long total,

            @Schema(description = "Чем пользуются посетители (тема, ширина сайта) за разные периоды")
            List<PreferencePeriodStats> preferences,

            @Schema(description = "Работа с инструментами сайта (например, ширмой Мастера) за разные периоды. "
                    + "Пока online-service не обновлён, поля нет — придёт null", nullable = true)
            List<FeatureUsageStats> usage
    ) {}

    @Schema(description = "Настройки отображения посетителей за период")
    public record PreferencePeriodStats(
            @Schema(description = "За сколько последних суток (включая сегодня) посчитаны посетители")
            int days,

            @Schema(description = "Имя настройки - значение - число уникальных посетителей")
            Map<String, Map<String, Long>> preferences
    ) {}

    @Schema(description = "Работа с одним инструментом сайта")
    public record FeatureUsageStats(
            @Schema(description = "Инструмент сайта", example = "dm-screen")
            String feature,

            @Schema(description = "Те же числа за разные периоды")
            List<UsagePeriodStats> periods
    ) {}

    /**
     * «Засчитанный день» посетителя — день, в который он набрал не меньше порога времени работы.
     * Порог задаётся в online-service; открыть страницу и уйти — не в счёт.
     */
    @Schema(description = "Работа с инструментом за период. «Засчитанный день» посетителя — день, в который "
            + "он проработал с инструментом не меньше порога, заданного в online-service")
    public record UsagePeriodStats(
            @Schema(description = "За сколько последних суток (включая сегодня) посчитан срез")
            int days,

            @Schema(description = "Сколько разных посетителей открывали инструмент")
            long visitors,

            @Schema(description = "Сколько из них действительно работали: есть хотя бы один засчитанный день")
            long users,

            @Schema(description = "Сколько возвращались: два и более засчитанных дня")
            long returning,

            @Schema(description = "Сколько всего засчитанных дней, сумма по посетителям")
            long activeDays,

            @Schema(description = "Сколько минут набрано за засчитанные дни, с округлением вниз")
            long activeMinutes
    ) {}

    @Schema(description = "Число посетителей онлайн")
    public record OnlineCount(
            @Schema(description = "Гости")
            long guests,

            @Schema(description = "Зарегистрированные пользователи")
            long registered,

            @Schema(description = "Сколько из них сейчас в игровых мирах; в total второй раз не складываются")
            long players,

            @Schema(description = "Всего: гости и зарегистрированные")
            long total
    ) {}

    @Schema(description = "Статистика онлайна для админки")
    public record OnlineAdminStatsResponse(
            @Schema(description = "За сколько последних минут посчитан онлайн")
            long windowMinutes,

            @Schema(description = "Онлайн по всем сайтам вместе")
            OnlineCount total,

            @Schema(description = "То же по каждому сайту, плюс настройки отображения и работа с инструментами")
            List<OnlineStatsResponse> sites
    ) {}

    /*
     * Дата и время — строками: online-service уже отдаёт их в нужном поясе, а пересборка через
     * java.time могла бы сдвинуть смещение (например, в UTC). Числа — обёртками: день без данных
     * приходит с null, и до админки должен дойти null, а не 0.
     */
    @Schema(description = "Посещаемость по суткам для админки")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record OnlineHistoryResponse(
            @Schema(description = "Часовой пояс, в котором считаются сутки", example = "Europe/Moscow")
            String zone,

            @Schema(description = "Первый день периода", example = "2026-10-18")
            String from,

            @Schema(description = "Последний день периода, сегодня", example = "2026-11-16")
            String to,

            @Schema(description = "По всем сайтам вместе")
            OnlineHistorySeries total,

            @Schema(description = "То же по каждому сайту")
            List<OnlineSiteHistory> sites
    ) {}

    @Schema(description = "Посещаемость по дням")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record OnlineHistorySeries(
            @Schema(description = "Дни периода по порядку")
            List<OnlineHistoryDay> days
    ) {}

    @Schema(description = "Посещаемость одного сайта по дням")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record OnlineSiteHistory(
            @Schema(description = "Сайт", example = "5e14")
            String siteId,

            @Schema(description = "Дни периода по порядку")
            List<OnlineHistoryDay> days
    ) {}

    @Schema(description = "Посещаемость за одни сутки. В день без данных числа и peakAt — null")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record OnlineHistoryDay(
            @Schema(description = "День", example = "2026-11-16")
            String date,

            @Schema(description = "Разных посетителей за сутки", nullable = true)
            Long visitors,

            @Schema(description = "Из них гостей", nullable = true)
            Long guests,

            @Schema(description = "Из них зарегистрированных", nullable = true)
            Long registered,

            @Schema(description = "Наибольший онлайн за сутки", nullable = true)
            Long peakOnline,

            @Schema(description = "Когда был наибольший онлайн", example = "2026-11-16T21:35:00+03:00",
                    nullable = true)
            String peakAt,

            @Schema(description = "Средний онлайн за сутки", nullable = true)
            Double averageOnline,

            @Schema(description = "Наибольшее число посетителей в игровых мирах за сутки", nullable = true)
            Long peakPlayers
    ) {}
}

