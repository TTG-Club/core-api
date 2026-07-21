package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.subscription.service.SubscriptionStatusClient;
import club.ttg.dnd5.domain.subscription.service.SubscriptionStatusClient.SubscriptionStatus;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Решает, какой объём контента отдавать на экспорт VTTG: весь ({@code srdOnly = false})
 * или только SRD ({@code srdOnly = true}). Вычисляется на каждый запрос.
 * <p>
 * Статус подписки берётся из subscriber-service через {@link SubscriptionStatusClient} —
 * по факту <b>действующей подписки</b>, а НЕ по роли {@code SUBSCRIBER} из токена: роль
 * кэшируется в JWT и остаётся до перелогина. Если бы доступ зависел от роли, истёкшая
 * подписка всё равно открывала бы весь контент.
 * <p>
 * Fail-closed: при недоступности subscriber-service подписка считается отсутствующей —
 * полный контент в этом случае не отдаётся никогда. Админ проверяется по JWT <b>до</b>
 * вызова, поэтому от недоступности subscriber-service не страдает.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VttgAccessService {
    private final SubscriptionStatusClient subscriptionStatusClient;

    /**
     * Вычисляет объём контента для текущего пользователя. Админ — всегда полный;
     * для остальных полный объём только при действующей подписке, иначе SRD.
     *
     * @return {@code srdOnly = false} для полного контента, {@code srdOnly = true} для SRD
     * @throws ApiException 403, если у пользователя без раннего доступа нет даже
     *                      зарегистрированной подписки
     */
    public VttgAccess access() {
        boolean admin = SecurityUtils.userRoles().anyMatch("ADMIN"::equals);
        if (admin) {
            return new VttgAccess(false);
        }

        String username = SecurityUtils.getUser().getUsername();
        SubscriptionStatus status = subscriptionStatusClient.fetch(username);

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
     * Результат проверки доступа: {@code srdOnly = true} — отдавать только SRD,
     * {@code false} — весь контент.
     */
    public record VttgAccess(boolean srdOnly) {
    }
}
