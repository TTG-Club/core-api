package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.subscription.service.SubscriptionService;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class VttgAccessService {
    private final SubscriptionService subscriptionService;

    /**
     * Решает, какой объём контента отдавать: весь ({@code srdOnly = false}) или только SRD
     * ({@code srdOnly = true}). Вычисляется на каждый запрос.
     * <p>
     * Полноту контента определяем по факту <b>действующей подписки в БД</b> (явная проверка
     * окончания срока), а НЕ по роли {@code SUBSCRIBER} из токена: роль кэшируется в JWT и
     * остаётся до перелогина/обновления токена. Если бы доступ зависел от роли, истёкшая
     * подписка всё равно открывала бы весь контент, пока пользователь не разлогинится.
     * Поэтому роль здесь сознательно игнорируется.
     */
    public VttgAccess access() {
        boolean admin = SecurityUtils.userRoles().anyMatch("ADMIN"::equals);
        if (admin) {
            return new VttgAccess(false);
        }

        String username = SecurityUtils.getUser().getUsername();

        // Подписка действует, только если уже стартовала и срок ещё не истёк (expiresAt > now).
        // Не закончилась — отдаём весь контент.
        boolean active = subscriptionService.hasActiveSubscription(username, Instant.now());
        if (active) {
            return new VttgAccess(false);
        }

        // Подписка закончилась (или ещё не активирована): дальше только SRD.
        boolean earlyAccess = SecurityUtils.userRoles().anyMatch("VTTG"::equals);
        if (!earlyAccess && !subscriptionService.hasRegisteredSubscription(username)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Для экспорта VTTG нужна зарегистрированная подписка");
        }

        return new VttgAccess(true);
    }

    public record VttgAccess(boolean srdOnly) {
    }
}
