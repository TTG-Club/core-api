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

    public VttgAccess access() {
        boolean admin = SecurityUtils.userRoles().anyMatch("ADMIN"::equals);
        if (admin) {
            return new VttgAccess(false);
        }

        boolean earlyAccess = SecurityUtils.userRoles().anyMatch("VTTG"::equals);
        String username = SecurityUtils.getUser().getUsername();
        boolean active = subscriptionService.hasActiveSubscription(username, Instant.now());
        if (active) {
            return new VttgAccess(false);
        }

        if (!earlyAccess && !subscriptionService.hasRegisteredSubscription(username)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Для экспорта VTTG нужна зарегистрированная подписка");
        }

        return new VttgAccess(true);
    }

    public record VttgAccess(boolean srdOnly) {
    }
}
