package club.ttg.dnd5.domain.tool.sheet.service;

import club.ttg.dnd5.domain.subscription.client.SubscriptionStatusClient;
import club.ttg.dnd5.domain.subscription.client.SubscriptionStatusClient.SubscriptionStatus;
import club.ttg.dnd5.domain.user.model.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Расчёт лимитов по подписке: базовые, расширенные и поведение при недоступном subscriber-service.
 */
class CharacterSheetLimitsTest {

    private final SubscriptionStatusClient subscriptionStatusClient = mock(SubscriptionStatusClient.class);
    private final CharacterSheetLimits limits = new CharacterSheetLimits(subscriptionStatusClient);

    @Test
    void subscriberLimitsAreReportedRegardlessOfSubscription() {
        // Ими клиент подсказывает, что даст подписка, поэтому статус тут не спрашивается
        SheetLimits actual = limits.subscriberLimits();

        assertEquals(20, actual.activeSheets());
        assertEquals(40, actual.savedSheets());
        assertEquals(30, actual.deletedHistory());
        verifyNoInteractions(subscriptionStatusClient);
    }

    @Test
    void userWithoutSubscriptionGetsBaseLimits() {
        User user = user();
        when(subscriptionStatusClient.status(user.getUsername()))
                .thenReturn(Optional.of(new SubscriptionStatus(false, true, null, null, null)));

        SheetLimits actual = limits.forUser(user);

        assertEquals(8, actual.activeSheets());
        assertEquals(16, actual.savedSheets());
        assertEquals(20, actual.deletedHistory());
        assertEquals(20, actual.deletedHistoryToTrim());
    }

    @Test
    void activeSubscriptionRaisesLimits() {
        User user = user();
        when(subscriptionStatusClient.status(user.getUsername()))
                .thenReturn(Optional.of(new SubscriptionStatus(true, true, null, null, null)));

        SheetLimits actual = limits.forUser(user);

        assertEquals(20, actual.activeSheets());
        assertEquals(40, actual.savedSheets());
        assertEquals(30, actual.deletedHistory());
        assertEquals(30, actual.deletedHistoryToTrim());
    }

    @Test
    void unknownStatusKeepsBaseLimitsButSparesHistory() {
        User user = user();
        when(subscriptionStatusClient.status(user.getUsername())).thenReturn(Optional.empty());

        SheetLimits actual = limits.forUser(user);

        // Платные лимиты наугад не выдаём...
        assertEquals(8, actual.activeSheets());
        assertEquals(16, actual.savedSheets());
        assertEquals(20, actual.deletedHistory());
        // ...но вытеснение необратимо, поэтому историю подписчика сбой subscriber-service не стирает
        assertEquals(30, actual.deletedHistoryToTrim());
    }

    private static User user() {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setUsername("gimli");
        return user;
    }
}
