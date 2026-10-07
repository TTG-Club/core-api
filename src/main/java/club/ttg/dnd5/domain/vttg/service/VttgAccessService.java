package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.subscription.client.SubscriptionStatusClient;
import club.ttg.dnd5.domain.subscription.client.SubscriptionStatusClient.SubscriptionStatus;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Решает, с какой автоматизацией отдавать экспорт VTTG. Сами записи отдаются всем целиком;
 * от подписки зависит только, едут ли с записями вне SRD их активные эффекты
 * ({@link VttgAutomation#FULL}) или эффекты остаются лишь у SRD ({@link VttgAutomation#SRD}).
 * Вычисляется на каждый запрос.
 * <p>
 * Статус подписки берётся из subscriber-service (см. {@link SubscriptionStatusClient}).
 * Автоматизацию определяем по факту <b>действующей подписки</b>, а НЕ по роли
 * {@code SUBSCRIBER} из токена: роль кэшируется в JWT и остаётся до перелогина.
 * Если бы доступ зависел от роли, истёкшая подписка всё равно открывала бы автоматизацию.
 * <p>
 * Fail-closed: при любой ошибке вызова subscriber-service (недоступен/таймаут/не-2xx)
 * считаем подписку отсутствующей ({@code active = false, registered = false}) — полная
 * автоматизация в этом случае не отдаётся никогда. Админ проверяется по JWT <b>до</b> вызова,
 * поэтому от недоступности subscriber-service не страдает.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VttgAccessService {
    private final SubscriptionStatusClient subscriptionStatusClient;

    /**
     * Вычисляет объём автоматизации для текущего пользователя. Админ — всегда полный;
     * для остальных полный только при действующей подписке, иначе эффекты лишь у SRD.
     *
     * @throws ApiException 403, если у пользователя без раннего доступа нет даже
     *                      зарегистрированной подписки
     */
    public VttgAccess access() {
        boolean admin = SecurityUtils.userRoles().anyMatch("ADMIN"::equals);
        if (admin) {
            return new VttgAccess(VttgAutomation.FULL);
        }

        String username = SecurityUtils.getUser().getUsername();
        SubscriptionStatus status = subscriptionStatusClient.status(username)
                .orElseGet(SubscriptionStatus::denied);

        // Подписка действует, только если уже стартовала и срок ещё не истёк — эффекты у всех записей.
        if (status.active()) {
            return new VttgAccess(VttgAutomation.FULL);
        }

        // Подписка закончилась (или ещё не активирована): записи те же, эффекты только у SRD.
        boolean earlyAccess = SecurityUtils.userRoles().anyMatch("VTTG"::equals);
        if (!earlyAccess && !status.registered()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Для экспорта VTTG нужна зарегистрированная подписка");
        }

        return new VttgAccess(VttgAutomation.SRD);
    }

    /** Результат проверки доступа: с какой автоматизацией отдавать записи. */
    public record VttgAccess(VttgAutomation automation) {
    }
}
