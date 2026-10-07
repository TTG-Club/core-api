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
 * <p>
 * Роль на автоматизацию не влияет, правило одно для всех: админ без действующей подписки
 * получает эффекты только у SRD, как и любой другой. Иначе клиент VTTG, который судит о
 * подписке сам, прятал бы у админа записи вне SRD: сервер упорно присылал бы их с эффектами.
 * Роли {@code ADMIN} и {@code VTTG} дают лишь ранний доступ — право на экспорт без
 * зарегистрированной подписки.
 * <p>
 * Fail-closed: при любой ошибке вызова subscriber-service (недоступен/таймаут/не-2xx)
 * считаем подписку отсутствующей ({@code active = false, registered = false}) — полная
 * автоматизация в этом случае не отдаётся никогда, в том числе админу.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VttgAccessService {
    private final SubscriptionStatusClient subscriptionStatusClient;

    /**
     * Вычисляет объём автоматизации для текущего пользователя: полный только при
     * действующей подписке, иначе эффекты лишь у SRD. От роли объём не зависит.
     *
     * @throws ApiException 403, если у пользователя без раннего доступа нет даже
     *                      зарегистрированной подписки
     */
    public VttgAccess access() {
        String username = SecurityUtils.getUser().getUsername();
        SubscriptionStatus status = subscriptionStatusClient.status(username)
                .orElseGet(SubscriptionStatus::denied);

        // Подписка действует, только если уже стартовала и срок ещё не истёк — эффекты у всех записей.
        if (status.active()) {
            return new VttgAccess(VttgAutomation.FULL);
        }

        // Подписка закончилась (или ещё не активирована): записи те же, эффекты только у SRD.
        boolean earlyAccess = SecurityUtils.userRoles()
                .anyMatch(role -> "VTTG".equals(role) || "ADMIN".equals(role));
        if (!earlyAccess && !status.registered()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Для экспорта VTTG нужна зарегистрированная подписка");
        }

        return new VttgAccess(VttgAutomation.SRD);
    }

    /** Результат проверки доступа: с какой автоматизацией отдавать записи. */
    public record VttgAccess(VttgAutomation automation) {
    }
}
