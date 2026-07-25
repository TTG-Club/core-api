package club.ttg.dnd5.domain.tool.sheet.service;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.repository.CharacterSheetRepository;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetPublicResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetShareResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.mapper.CharacterSheetMapper;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Доступ к листу по ссылке: выпуск и отзыв токена владельцем и анонимный просмотр.
 */
class CharacterSheetServiceTest {

    private final CharacterSheetRepository sheetRepository = mock(CharacterSheetRepository.class);
    private final CharacterSheetMapper sheetMapper = mock(CharacterSheetMapper.class);
    private final CharacterSheetService service = new CharacterSheetService(sheetRepository, sheetMapper);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shareIssuesTokenAndKeepsItOnRepeatedCalls() {
        UUID owner = authenticate();
        CharacterSheet sheet = sheet(owner);
        when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));

        CharacterSheetShareResponse first = service.share(sheet.getId());
        CharacterSheetShareResponse second = service.share(sheet.getId());

        assertNotNull(first.getShareToken());
        // Идемпотентность: перевыпуск токена сломал бы уже разосланные ссылки
        assertEquals(first.getShareToken(), second.getShareToken());
        assertEquals(first.getShareToken(), sheet.getShareToken());
    }

    @Test
    void shareOnForeignSheetIsForbidden() {
        authenticate();
        CharacterSheet foreign = sheet(UUID.randomUUID());
        when(sheetRepository.findById(foreign.getId())).thenReturn(Optional.of(foreign));

        ApiException exception = assertThrows(ApiException.class, () -> service.share(foreign.getId()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertNull(foreign.getShareToken());
    }

    @Test
    void revokeShareClearsToken() {
        UUID owner = authenticate();
        CharacterSheet sheet = sheet(owner);
        sheet.setShareToken(UUID.randomUUID());
        when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));

        service.revokeShare(sheet.getId());

        assertNull(sheet.getShareToken());
    }

    @Test
    void findSharedReturnsSheetToAnonymousViewer() {
        CharacterSheet sheet = sheet(UUID.randomUUID());
        UUID token = UUID.randomUUID();
        sheet.setShareToken(token);
        CharacterSheetPublicResponse expected = new CharacterSheetPublicResponse();
        when(sheetRepository.findByShareTokenAndDeletedFalse(token)).thenReturn(Optional.of(sheet));
        when(sheetMapper.toPublicResponse(sheet)).thenReturn(expected);

        // Контекст безопасности пуст — ручка публичная
        CharacterSheetPublicResponse actual = service.findShared(token.toString());

        assertSame(expected, actual);
    }

    @Test
    void findSharedWithUnknownTokenIsNotFound() {
        UUID token = UUID.randomUUID();
        when(sheetRepository.findByShareTokenAndDeletedFalse(token)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findShared(token.toString()));
    }

    @Test
    void findSharedWithMalformedTokenIsNotFoundInsteadOfServerError() {
        assertThrows(EntityNotFoundException.class, () -> service.findShared("не-похоже-на-uuid"));
        assertThrows(EntityNotFoundException.class, () -> service.findShared("  "));

        verify(sheetRepository, never()).findByShareTokenAndDeletedFalse(any());
    }

    private static CharacterSheet sheet(UUID ownerId) {
        CharacterSheet sheet = new CharacterSheet();
        sheet.setId(UUID.randomUUID());
        sheet.setUserId(ownerId);
        sheet.setName("Гимли");
        sheet.setData(JsonNodeFactory.instance.objectNode());
        return sheet;
    }

    private static UUID authenticate() {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
        return user.getUuid();
    }
}
