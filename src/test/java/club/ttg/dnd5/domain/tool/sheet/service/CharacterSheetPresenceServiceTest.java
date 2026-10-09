package club.ttg.dnd5.domain.tool.sheet.service;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetPresence;
import club.ttg.dnd5.domain.tool.sheet.repository.CharacterSheetPresenceRepository;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetPresenceResponse;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.rest.dto.DisplayNameByUserIdResponse;
import club.ttg.dnd5.domain.user.service.DisplayNameService;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Мягкая блокировка листа: отметка присутствия, список остальных и доступ к отметке.
 */
class CharacterSheetPresenceServiceTest {

    private final CharacterSheetService sheetService = mock(CharacterSheetService.class);
    private final CharacterSheetPresenceRepository presenceRepository = mock(CharacterSheetPresenceRepository.class);
    private final DisplayNameService displayNameService = mock(DisplayNameService.class);
    private final CharacterSheetPresenceService service =
            new CharacterSheetPresenceService(sheetService, presenceRepository, displayNameService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void heartbeatMarksPresenceAndReturnsOthersWithNames() {
        User user = authenticate();
        CharacterSheet sheet = sheet();
        UUID otherId = UUID.randomUUID();
        when(sheetService.getEditableActive(sheet.getId(), user)).thenReturn(sheet);
        when(presenceRepository.findAllBySheetIdAndUserIdNotAndSeenAtAfter(eq(sheet.getId()), eq(user.getUuid()),
                any())).thenReturn(List.of(presence(sheet, otherId)));
        when(displayNameService.resolveByUserIds(List.of(otherId)))
                .thenReturn(List.of(new DisplayNameByUserIdResponse(otherId, "Мастер", "https://avatar")));

        CharacterSheetPresenceResponse response = service.heartbeat(sheet.getId());

        verify(presenceRepository).upsert(any(), eq(sheet.getId()), eq(user.getUuid()), any());
        verify(presenceRepository).deleteAllBySheetIdAndSeenAtBefore(eq(sheet.getId()), any());
        assertEquals(1, response.getUsers().size());
        assertEquals("Мастер", response.getUsers().getFirst().getDisplayName());
        assertEquals("https://avatar", response.getUsers().getFirst().getAvatarUrl());
    }

    @Test
    void heartbeatAloneReturnsNobodyWithoutResolvingNames() {
        User user = authenticate();
        CharacterSheet sheet = sheet();
        when(sheetService.getEditableActive(sheet.getId(), user)).thenReturn(sheet);

        CharacterSheetPresenceResponse response = service.heartbeat(sheet.getId());

        assertTrue(response.getUsers().isEmpty());
        verify(displayNameService, never()).resolveByUserIds(any());
    }

    @Test
    void userWithoutDisplayNameGetsCommonLabel() {
        User user = authenticate();
        CharacterSheet sheet = sheet();
        UUID otherId = UUID.randomUUID();
        when(sheetService.getEditableActive(sheet.getId(), user)).thenReturn(sheet);
        when(presenceRepository.findAllBySheetIdAndUserIdNotAndSeenAtAfter(eq(sheet.getId()), eq(user.getUuid()),
                any())).thenReturn(List.of(presence(sheet, otherId)));
        when(displayNameService.resolveByUserIds(List.of(otherId))).thenReturn(List.of());

        CharacterSheetPresenceResponse response = service.heartbeat(sheet.getId());

        assertEquals("Пользователь без имени", response.getUsers().getFirst().getDisplayName());
        assertNull(response.getUsers().getFirst().getAvatarUrl());
    }

    @Test
    void heartbeatWithoutAccessIsRejectedAndMarksNothing() {
        User user = authenticate();
        UUID sheetId = UUID.randomUUID();
        when(sheetService.getEditableActive(sheetId, user))
                .thenThrow(new ApiException(HttpStatus.FORBIDDEN, "Доступ к листу персонажа запрещен"));

        ApiException exception = assertThrows(ApiException.class, () -> service.heartbeat(sheetId));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(presenceRepository, never()).upsert(any(), any(), any(), any());
    }

    @Test
    void leaveRemovesOnlyOwnMark() {
        User user = authenticate();
        UUID sheetId = UUID.randomUUID();

        service.leave(sheetId);

        verify(presenceRepository).deleteBySheetIdAndUserId(sheetId, user.getUuid());
    }

    private static CharacterSheet sheet() {
        CharacterSheet sheet = new CharacterSheet();
        sheet.setId(UUID.randomUUID());
        sheet.setUserId(UUID.randomUUID());
        return sheet;
    }

    private static CharacterSheetPresence presence(CharacterSheet sheet, UUID userId) {
        CharacterSheetPresence presence = new CharacterSheetPresence();
        presence.setId(UUID.randomUUID());
        presence.setSheetId(sheet.getId());
        presence.setUserId(userId);
        presence.setSeenAt(Instant.now());
        return presence;
    }

    private static User authenticate() {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
        return user;
    }
}
