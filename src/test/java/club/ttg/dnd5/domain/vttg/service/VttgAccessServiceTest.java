package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.subscription.service.SubscriptionService;
import club.ttg.dnd5.domain.user.model.Role;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VttgAccessServiceTest {
    private final SubscriptionService subscriptionService = mock(SubscriptionService.class);
    private final VttgAccessService service = new VttgAccessService(subscriptionService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminGetsFullExportWithoutSubscription() {
        authenticate("admin", "ADMIN");

        assertFalse(service.access().srdOnly());
    }

    @Test
    void earlyAccessVttgRoleGetsOnlySrdWithoutActiveSubscription() {
        authenticate("early", "VTTG");
        when(subscriptionService.hasActiveSubscription(eq("early"), any())).thenReturn(false);

        assertTrue(service.access().srdOnly());
    }

    @Test
    void activeSubscriptionGetsFullExport() {
        authenticate("subscriber", "USER");
        when(subscriptionService.hasActiveSubscription(eq("subscriber"), any())).thenReturn(true);

        assertFalse(service.access().srdOnly());
    }

    @Test
    void registeredInactiveSubscriptionGetsOnlySrd() {
        authenticate("registered", "USER");
        when(subscriptionService.hasActiveSubscription(eq("registered"), any())).thenReturn(false);
        when(subscriptionService.hasRegisteredSubscription("registered")).thenReturn(true);

        assertTrue(service.access().srdOnly());
    }

    @Test
    void userWithoutSubscriptionIsRejected() {
        authenticate("user", "USER");
        when(subscriptionService.hasActiveSubscription(eq("user"), any())).thenReturn(false);
        when(subscriptionService.hasRegisteredSubscription("user")).thenReturn(false);

        assertThrows(ApiException.class, service::access);
    }

    private void authenticate(String username, String role) {
        User user = new User();
        user.setUsername(username);
        user.setRoles(List.of(Role.builder().name(role).build()));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }
}
