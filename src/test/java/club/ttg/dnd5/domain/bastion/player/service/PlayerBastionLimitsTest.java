package club.ttg.dnd5.domain.bastion.player.service;

import club.ttg.dnd5.domain.subscription.client.SubscriptionStatusClient;
import club.ttg.dnd5.domain.subscription.client.SubscriptionStatusClient.SubscriptionStatus;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.model.UserDisplayName;
import club.ttg.dnd5.domain.user.repository.UserDisplayNameRepository;
import club.ttg.dnd5.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerBastionLimitsTest {
    private final SubscriptionStatusClient subscriptions = mock(SubscriptionStatusClient.class);
    private final UserRepository users = mock(UserRepository.class);
    private final UserDisplayNameRepository displayNames = mock(UserDisplayNameRepository.class);
    private final PlayerBastionLimits limits = new PlayerBastionLimits(subscriptions, users, displayNames);

    private final UUID ownerId = UUID.randomUUID();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private static Optional<SubscriptionStatus> active() {
        return Optional.of(new SubscriptionStatus(true, true, null, null, null));
    }

    private void signIn(UUID id, String username) {
        User user = new User();
        user.setUuid(id);
        user.setUsername(username);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @Test
    void ownerSubscriptionComesFromTokenLogin() {
        signIn(ownerId, "owner");
        when(subscriptions.status("owner")).thenReturn(active());

        assertTrue(limits.isSubscriber(ownerId));
    }

    @Test
    void otherOwnerLoginComesFromDisplayName() {
        signIn(UUID.randomUUID(), "editor");
        UserDisplayName name = new UserDisplayName();
        name.setUserId(ownerId);
        name.setUsername("owner");
        when(displayNames.findById(ownerId)).thenReturn(Optional.of(name));
        when(subscriptions.status("owner")).thenReturn(active());

        assertTrue(limits.isSubscriber(ownerId));
    }

    @Test
    void unknownOwnerIsNotSubscriber() {
        signIn(UUID.randomUUID(), "editor");
        when(displayNames.findById(ownerId)).thenReturn(Optional.empty());
        when(users.findById(ownerId)).thenReturn(Optional.empty());

        assertFalse(limits.isSubscriber(ownerId));
    }
}
