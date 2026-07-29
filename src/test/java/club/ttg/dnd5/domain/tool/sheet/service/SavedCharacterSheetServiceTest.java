package club.ttg.dnd5.domain.tool.sheet.service;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.model.SavedCharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.repository.CharacterSheetRepository;
import club.ttg.dnd5.domain.tool.sheet.repository.SavedCharacterSheetRepository;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.SavedCharacterSheetListResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.SavedCharacterSheetResponse;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Сохранение чужих листов по ссылке: лимит, свой лист, повторное сохранение и потеря доступа.
 */
class SavedCharacterSheetServiceTest {

    private static final SheetLimits FREE_LIMITS = new SheetLimits(8, 16, 20, 20);
    private static final SheetLimits SUBSCRIBER_LIMITS = new SheetLimits(20, 40, 30, 30);

    private final SavedCharacterSheetRepository savedRepository = mock(SavedCharacterSheetRepository.class);
    private final CharacterSheetRepository sheetRepository = mock(CharacterSheetRepository.class);
    private final CharacterSheetLimits sheetLimits = mock(CharacterSheetLimits.class);
    private final SavedCharacterSheetService service =
            new SavedCharacterSheetService(savedRepository, sheetRepository, sheetLimits);

    @BeforeEach
    void withoutSubscriptionByDefault() {
        when(sheetLimits.forUser(any())).thenReturn(FREE_LIMITS);
        when(sheetLimits.subscriberLimits()).thenReturn(SUBSCRIBER_LIMITS);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void saveStoresForeignSheetByShareToken() {
        UUID viewer = authenticate();
        CharacterSheet sheet = sharedSheet();
        when(sheetRepository.findByShareTokenAndDeletedFalse(sheet.getShareToken()))
                .thenReturn(Optional.of(sheet));
        when(savedRepository.findByUserIdAndSheetId(viewer, sheet.getId())).thenReturn(Optional.empty());
        when(savedRepository.countByUserId(viewer)).thenReturn(0L);
        when(savedRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SavedCharacterSheetResponse response = service.save(sheet.getShareToken().toString());

        assertEquals(sheet.getId(), response.getSheetId());
        assertEquals(sheet.getShareToken(), response.getShareToken());
        assertEquals(sheet.getName(), response.getName());
        assertTrue(response.isAvailable());
    }

    @Test
    void saveOfOwnSheetIsRejected() {
        UUID owner = authenticate();
        CharacterSheet sheet = sharedSheet();
        sheet.setUserId(owner);
        when(sheetRepository.findByShareTokenAndDeletedFalse(sheet.getShareToken()))
                .thenReturn(Optional.of(sheet));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.save(sheet.getShareToken().toString()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(savedRepository, never()).saveAndFlush(any());
    }

    @Test
    void saveOfAlreadySavedSheetRefreshesTokenInsteadOfDuplicating() {
        UUID viewer = authenticate();
        CharacterSheet sheet = sharedSheet();
        SavedCharacterSheet existing = new SavedCharacterSheet();
        existing.setId(UUID.randomUUID());
        existing.setUserId(viewer);
        existing.setSheetId(sheet.getId());
        existing.setShareToken(UUID.randomUUID());
        existing.setName("Старое имя");
        when(sheetRepository.findByShareTokenAndDeletedFalse(sheet.getShareToken()))
                .thenReturn(Optional.of(sheet));
        when(savedRepository.findByUserIdAndSheetId(viewer, sheet.getId())).thenReturn(Optional.of(existing));
        when(savedRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SavedCharacterSheetResponse response = service.save(sheet.getShareToken().toString());

        assertEquals(existing.getId(), response.getId());
        assertEquals(sheet.getShareToken(), existing.getShareToken());
        assertEquals(sheet.getName(), existing.getName());
        // Лимит проверяется только для новых записей: освежить уже сохранённый лист можно всегда
        verify(savedRepository, never()).countByUserId(any());
    }

    @Test
    void saveOverLimitIsRejected() {
        UUID viewer = authenticate();
        CharacterSheet sheet = sharedSheet();
        when(sheetRepository.findByShareTokenAndDeletedFalse(sheet.getShareToken()))
                .thenReturn(Optional.of(sheet));
        when(savedRepository.findByUserIdAndSheetId(viewer, sheet.getId())).thenReturn(Optional.empty());
        when(savedRepository.countByUserId(viewer)).thenReturn(16L);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.save(sheet.getShareToken().toString()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(savedRepository, never()).saveAndFlush(any());
    }

    @Test
    void subscriberSavesBeyondBaseLimit() {
        UUID viewer = authenticate();
        CharacterSheet sheet = sharedSheet();
        when(sheetLimits.forUser(any())).thenReturn(SUBSCRIBER_LIMITS);
        when(sheetRepository.findByShareTokenAndDeletedFalse(sheet.getShareToken()))
                .thenReturn(Optional.of(sheet));
        when(savedRepository.findByUserIdAndSheetId(viewer, sheet.getId())).thenReturn(Optional.empty());
        when(savedRepository.countByUserId(viewer)).thenReturn(16L);
        when(savedRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SavedCharacterSheetResponse response = service.save(sheet.getShareToken().toString());

        assertEquals(sheet.getId(), response.getSheetId());
        verify(savedRepository).saveAndFlush(any());
    }

    @Test
    void saveWithMalformedTokenIsNotFoundInsteadOfServerError() {
        authenticate();

        assertThrows(EntityNotFoundException.class, () -> service.save("не-похоже-на-uuid"));

        verify(sheetRepository, never()).findByShareTokenAndDeletedFalse(any());
    }

    @Test
    void findMineMarksRevokedAndDeletedSheetsUnavailable() {
        UUID viewer = authenticate();
        CharacterSheet revoked = sharedSheet();
        SavedCharacterSheet savedRevoked = savedRecord(viewer, revoked);
        // Владелец отозвал доступ: токен листа обнулился, а сохранённый остался
        revoked.setShareToken(null);
        CharacterSheet alive = sharedSheet();
        SavedCharacterSheet savedAlive = savedRecord(viewer, alive);
        when(savedRepository.findAllByUserIdOrderByCreatedAtDesc(viewer))
                .thenReturn(List.of(savedRevoked, savedAlive));
        when(sheetRepository.findAllById(any())).thenReturn(List.of(revoked, alive));

        SavedCharacterSheetListResponse response = service.findMine();

        assertEquals(16, response.getLimit());
        // Лимит подписчика выше выданного — клиент покажет подсказку про подписку
        assertEquals(40, response.getSubscriberLimit());
        assertEquals(2, response.getCount());
        SavedCharacterSheetResponse first = response.getSheets().getFirst();
        assertFalse(first.isAvailable());
        assertNull(first.getData());
        // Название недоступного листа берётся из снимка — живое взять неоткуда
        assertEquals(savedRevoked.getName(), first.getName());
        assertTrue(response.getSheets().get(1).isAvailable());
    }

    @Test
    void deleteOfForeignRecordIsNotFound() {
        UUID viewer = authenticate();
        UUID savedId = UUID.randomUUID();
        when(savedRepository.findByIdAndUserId(savedId, viewer)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.delete(savedId));

        verify(savedRepository, never()).delete(any());
    }

    private static CharacterSheet sharedSheet() {
        CharacterSheet sheet = new CharacterSheet();
        sheet.setId(UUID.randomUUID());
        sheet.setUserId(UUID.randomUUID());
        sheet.setName("Гимли");
        sheet.setData(JsonNodeFactory.instance.objectNode());
        sheet.setShareToken(UUID.randomUUID());
        return sheet;
    }

    private static SavedCharacterSheet savedRecord(UUID userId, CharacterSheet sheet) {
        SavedCharacterSheet saved = new SavedCharacterSheet();
        saved.setId(UUID.randomUUID());
        saved.setUserId(userId);
        saved.setSheetId(sheet.getId());
        saved.setShareToken(sheet.getShareToken());
        saved.setName(sheet.getName());
        return saved;
    }

    private static UUID authenticate() {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
        return user.getUuid();
    }
}
