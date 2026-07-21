package club.ttg.dnd5.domain.subscription.service;

import club.ttg.dnd5.config.properties.InternalServiceProperties;
import club.ttg.dnd5.security.InternalServiceTokenFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;

/**
 * Статус подписки пользователя из subscriber-service. Домен подписок вынесен из core-api
 * в отдельный сервис, своих таблиц подписок здесь больше нет — единственный источник
 * правды это HTTP-вызов.
 * <p>
 * Статус спрашивается на каждый запрос и НЕ подменяется ролью {@code SUBSCRIBER} из
 * токена: роль кэшируется в JWT и живёт до перелогина, поэтому истёкшая подписка
 * продолжала бы открывать платные возможности.
 * <p>
 * Fail-closed: при любой ошибке вызова (недоступен/таймаут/не-2xx) считаем, что подписки
 * нет. Платное поведение при недоступности сервиса не включается никогда.
 */
@Slf4j
@Service
public class SubscriptionStatusClient {

    private final InternalServiceProperties internalProperties;
    private final RestClient restClient;

    public SubscriptionStatusClient(InternalServiceProperties internalProperties,
                                    RestClient subscriberServiceRestClient) {
        this.internalProperties = internalProperties;
        this.restClient = subscriberServiceRestClient;
    }

    /**
     * Запрашивает статус подписки. Любая ошибка → fail-closed:
     * {@code active = false, registered = false}.
     *
     * @param username Логин пользователя.
     */
    public SubscriptionStatus fetch(String username) {
        try {
            SubscriptionStatus status = restClient.get()
                    .uri("/api/internal/subscriptions/{username}/status", username)
                    .headers(this::addServiceHeaders)
                    .retrieve()
                    .body(SubscriptionStatus.class);

            return status == null ? SubscriptionStatus.denied() : status;
        } catch (RestClientException ex) {
            log.warn("Не удалось получить статус подписки {} из subscriber-service, fail-closed", username, ex);
            return SubscriptionStatus.denied();
        }
    }

    private void addServiceHeaders(HttpHeaders headers) {
        String secret = internalProperties.getServiceSecret();
        if (secret != null && !secret.isBlank()) {
            headers.set(InternalServiceTokenFilter.SERVICE_TOKEN_HEADER, secret);
        }
    }

    /**
     * Ответ subscriber-service. {@code active} — подписка стартовала и не истекла;
     * {@code registered} — подписка есть, но ещё не активирована.
     */
    public record SubscriptionStatus(boolean active, boolean registered,
                                     Instant expiresAt, Instant startsAt, String type) {
        public static SubscriptionStatus denied() {
            return new SubscriptionStatus(false, false, null, null, null);
        }
    }
}
