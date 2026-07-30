package club.ttg.dnd5.domain.tool.sheet.service;

import club.ttg.dnd5.domain.subscription.client.SubscriptionStatusClient;
import club.ttg.dnd5.domain.subscription.client.SubscriptionStatusClient.SubscriptionStatus;
import club.ttg.dnd5.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Считает лимиты листов персонажей: базовые для всех и расширенные для тех, у кого подписка
 * действует прямо сейчас (активирована и не истекла). Единственное место, где живёт таблица
 * лимитов, — своими и сохранёнными листами заведуют разные сервисы, но лимит у них общий по
 * происхождению.
 * <p>
 * Статус подписки берётся из subscriber-service на каждую операцию, а не из роли в токене:
 * роль в JWT живёт до перелогина, поэтому истёкшая подписка ещё долго держала бы расширенный
 * лимит. Недоступность сервиса — не подписка (fail-closed): расширенный лимит не выдаётся,
 * уже созданные сверх базового лимита листы при этом никуда не деваются, просто нельзя
 * создать новый до восстановления связи.
 */
@RequiredArgsConstructor
@Component
public class CharacterSheetLimits {

    /** Лимиты пользователя без действующей подписки. */
    private static final SheetLimits BASE = new SheetLimits(8, 16, 20, 20);

    /** Лимиты по действующей подписке. */
    private static final SheetLimits SUBSCRIBER = new SheetLimits(20, 40, 30, 30);

    private final SubscriptionStatusClient subscriptionStatusClient;

    /**
     * Лимиты пользователя. Один поход в subscriber-service на вызов — результат забирается целиком
     * и раздаётся всем лимитам операции.
     */
    public SheetLimits forUser(User user) {
        Optional<SubscriptionStatus> status = subscriptionStatusClient.status(user.getUsername());
        if (status.map(SubscriptionStatus::active).orElse(false)) {
            return SUBSCRIBER;
        }
        if (status.isEmpty()) {
            // Статус неизвестен: платные лимиты наугад не выдаём, но историю бережём по максимуму —
            // вытеснение необратимо, а сбой subscriber-service не должен стоить подписчику листов.
            return new SheetLimits(BASE.activeSheets(), BASE.savedSheets(), BASE.deletedHistory(),
                    SUBSCRIBER.deletedHistory());
        }
        return BASE;
    }

    /**
     * Лимиты, которые даёт подписка, — независимо от того, есть ли она у пользователя. Клиент
     * показывает их подсказкой «с подпиской доступно больше», поэтому числа отдаёт сервер:
     * на клиенте они не хардкодятся.
     */
    public SheetLimits subscriberLimits() {
        return SUBSCRIBER;
    }
}
