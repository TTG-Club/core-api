package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.config.properties.InternalServiceProperties;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.security.InternalServiceTokenFilter;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;

/**
 * Решает, какой объём контента отдавать на экспорт VTTG: весь ({@code srdOnly = false})
 * или только SRD ({@code srdOnly = true}). Вычисляется на каждый запрос.
 * <p>
 * Статус подписки берётся из subscriber-service (домен подписок вынесен в отдельный
 * сервис). Полноту контента определяем по факту <b>действующей подписки</b>, а НЕ по
 * роли {@code SUBSCRIBER} из токена: роль кэшируется в JWT и остаётся до перелогина.
 * Если бы доступ зависел от роли, истёкшая подписка всё равно открывала бы весь контент.
 * <p>
 * Fail-closed: при любой ошибке вызова subscriber-service (недоступен/таймаут/не-2xx)
 * считаем подписку отсутствующей ({@code active = false, registered = false}) — полный
 * контент в этом случае не отдаётся никогда. Админ проверяется по JWT <b>до</b> вызова,
 * поэтому от недоступности subscriber-service не страдает.
 */
@Slf4j
@Service
public class VttgAccessService {
    private final InternalServiceProperties internalProperties;
    private final RestClient restClient;

    public VttgAccessService(InternalServiceProperties internalProperties,
                             RestClient subscriberServiceRestClient) {
        this.internalProperties = internalProperties;
        this.restClient = subscriberServiceRestClient;
    }

    public VttgAccess access() {
        boolean admin = SecurityUtils.userRoles().anyMatch("ADMIN"::equals);
        if (admin) {
            return new VttgAccess(false);
        }

        String username = SecurityUtils.getUser().getUsername();
        SubscriptionStatus status = fetchStatus(username);

        // Подписка действует, только если уже стартовала и срок ещё не истёк — отдаём весь контент.
        if (status.active()) {
            return new VttgAccess(false);
        }

        // Подписка закончилась (или ещё не активирована): дальше только SRD.
        boolean earlyAccess = SecurityUtils.userRoles().anyMatch("VTTG"::equals);
        if (!earlyAccess && !status.registered()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Для экспорта VTTG нужна зарегистрированная подписка");
        }

        return new VttgAccess(true);
    }

    /**
     * Запрашивает статус подписки в subscriber-service. Любая ошибка → fail-closed:
     * {@code active = false, registered = false}.
     */
    private SubscriptionStatus fetchStatus(String username) {
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
     * Ответ subscriber-service. Нас интересуют только {@code active}/{@code registered};
     * остальные поля контракта десериализуются, но в логике доступа не используются.
     */
    public record SubscriptionStatus(boolean active, boolean registered,
                                     Instant expiresAt, Instant startsAt, String type) {
        static SubscriptionStatus denied() {
            return new SubscriptionStatus(false, false, null, null, null);
        }
    }

    public record VttgAccess(boolean srdOnly) {
    }
}
