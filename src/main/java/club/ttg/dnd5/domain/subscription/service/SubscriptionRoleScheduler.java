package club.ttg.dnd5.domain.subscription.service;

import club.ttg.dnd5.domain.user.model.Role;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Снимает роль {@link SubscriptionService#SUBSCRIBER_ROLE} с пользователей, у которых
 * не осталось ни одной действующей подписки. Выдаётся роль при активации
 * ({@link SubscriptionService#activate}), а истечение по времени отслеживается здесь.
 * <p>
 * Роль в JWT кэшируется до перелогина/обновления токена, поэтому источником истины
 * остаётся БД: снятие здесь применится к доступам при следующем получении токена.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SubscriptionRoleScheduler {

    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    /**
     * Ежедневно в 03:30 по серверному времени.
     */
    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void revokeExpiredSubscriberRoles() {
        Instant now = Instant.now();
        List<User> subscribers = userRepository.findAllByRoleName(SubscriptionService.SUBSCRIBER_ROLE);
        int revoked = 0;
        for (User user : subscribers) {
            if (subscriptionService.hasActiveSubscription(user.getUsername(), now)) {
                continue;
            }
            List<Role> roles = user.getRoles().stream()
                    .filter(role -> !SubscriptionService.SUBSCRIBER_ROLE.equals(role.getName()))
                    .toList();
            user.setRoles(roles);
            userRepository.save(user);
            revoked++;
        }
        if (revoked > 0) {
            log.info("Снята роль {} у {} пользователей без активной подписки",
                    SubscriptionService.SUBSCRIBER_ROLE, revoked);
        }
    }
}
