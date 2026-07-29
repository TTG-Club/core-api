package club.ttg.dnd5.domain.subscription.client;

import club.ttg.dnd5.config.properties.InternalServiceProperties;
import club.ttg.dnd5.security.InternalServiceTokenFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.Optional;

/**
 * Статус подписки пользователя из subscriber-service: домен подписок вынесен туда целиком,
 * в core-api своей таблицы подписок нет.
 * <p>
 * Проверять подписку нужно именно здесь, а не по роли {@code SUBSCRIBER} из токена: роль
 * кэшируется в JWT и живёт до перелогина, поэтому истёкшая подписка продолжала бы открывать
 * платные возможности.
 * <p>
 * Недоступность сервиса (таймаут, не-2xx, пустое тело) — это {@code Optional.empty()}, а не
 * «подписки нет»: вызывающий сам решает, чем обернуть неизвестный статус. Для выдачи платных
 * возможностей это отказ (fail-closed), а для необратимых операций вроде вытеснения истории —
 * повод считать пользователя подписчиком, чтобы чужой сбой не стоил ему данных.
 */
@Slf4j
@Component
public class SubscriptionStatusClient {

    private final InternalServiceProperties internalProperties;
    private final RestClient restClient;

    public SubscriptionStatusClient(InternalServiceProperties internalProperties,
                                    RestClient subscriberServiceRestClient) {
        this.internalProperties = internalProperties;
        this.restClient = subscriberServiceRestClient;
    }

    /**
     * Реал-тайм статус подписки. Идентификатор пользователя в subscriber-service — его username.
     *
     * @return статус или {@code Optional.empty()}, если subscriber-service не ответил
     */
    public Optional<SubscriptionStatus> status(String username) {
        if (!StringUtils.hasText(username)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/api/internal/subscriptions/{username}/status", username)
                    .headers(this::addServiceHeaders)
                    .retrieve()
                    .body(SubscriptionStatus.class));
        } catch (RestClientException ex) {
            log.warn("Не удалось получить статус подписки {} из subscriber-service", username, ex);
            return Optional.empty();
        }
    }

    private void addServiceHeaders(HttpHeaders headers) {
        String secret = internalProperties.getServiceSecret();
        if (secret != null && !secret.isBlank()) {
            headers.set(InternalServiceTokenFilter.SERVICE_TOKEN_HEADER, secret);
        }
    }

    /**
     * Ответ subscriber-service.
     *
     * @param active     подписка действует прямо сейчас: активирована ({@code startsAt} проставлен)
     *                   и срок ещё не истёк
     * @param registered есть хоть какая-то подписка, в том числе не активированная
     * @param expiresAt  когда заканчивается действующая подписка ({@code null}, если её нет)
     * @param startsAt   когда активирована действующая подписка ({@code null}, если её нет)
     * @param type       тип действующей подписки ({@code null}, если её нет)
     */
    public record SubscriptionStatus(boolean active, boolean registered,
                                     Instant expiresAt, Instant startsAt, String type) {
        /** Статус «подписки нет»: им подменяется неизвестный статус там, где нужен fail-closed. */
        public static SubscriptionStatus denied() {
            return new SubscriptionStatus(false, false, null, null, null);
        }
    }
}
