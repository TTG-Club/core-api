package club.ttg.dnd5.domain.tool.sheet.service;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetEditor;
import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetEditorStatus;
import club.ttg.dnd5.domain.tool.sheet.model.SavedCharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.repository.CharacterSheetEditorRepository;
import club.ttg.dnd5.domain.tool.sheet.repository.CharacterSheetRepository;
import club.ttg.dnd5.domain.tool.sheet.repository.SavedCharacterSheetRepository;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetEditAccessResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetEditRequestResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetEditorListResponse;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.rest.dto.DisplayNameByUserIdResponse;
import club.ttg.dnd5.domain.user.service.DisplayNameService;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Права на редактирование листа: запрос по сохранённой ссылке, пауза после отказа, решение владельца
 * и лимит редакторов.
 */
class CharacterSheetEditorServiceTest {

    private final CharacterSheetEditorRepository editorRepository = mock(CharacterSheetEditorRepository.class);
    private final CharacterSheetRepository sheetRepository = mock(CharacterSheetRepository.class);
    private final SavedCharacterSheetRepository savedRepository = mock(SavedCharacterSheetRepository.class);
    private final DisplayNameService displayNameService = mock(DisplayNameService.class);
    private final CharacterSheetEditorService service = new CharacterSheetEditorService(
            editorRepository, sheetRepository, savedRepository, displayNameService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requestBySavedLinkCreatesPendingRequest() {
        UUID requester = authenticate();
        CharacterSheet sheet = sharedSheet(UUID.randomUUID());
        SavedCharacterSheet saved = savedRecord(requester, sheet);
        when(savedRepository.findByIdAndUserId(saved.getId(), requester)).thenReturn(Optional.of(saved));
        when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));
        when(editorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CharacterSheetEditRequestResponse response = service.requestEdit(saved.getId());

        assertEquals(CharacterSheetEditorStatus.PENDING, response.getStatus());
    }

    @Test
    void requestByRevokedLinkIsNotFound() {
        UUID requester = authenticate();
        CharacterSheet sheet = sharedSheet(UUID.randomUUID());
        SavedCharacterSheet saved = savedRecord(requester, sheet);
        sheet.setShareToken(UUID.randomUUID());
        when(savedRepository.findByIdAndUserId(saved.getId(), requester)).thenReturn(Optional.of(saved));
        when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));

        assertThrows(EntityNotFoundException.class, () -> service.requestEdit(saved.getId()));

        verify(editorRepository, never()).save(any());
    }

    @Test
    void repeatedRequestKeepsGrantedRight() {
        UUID requester = authenticate();
        CharacterSheet sheet = sharedSheet(UUID.randomUUID());
        SavedCharacterSheet saved = savedRecord(requester, sheet);
        CharacterSheetEditor editor = editor(sheet, requester, CharacterSheetEditorStatus.APPROVED);
        when(savedRepository.findByIdAndUserId(saved.getId(), requester)).thenReturn(Optional.of(saved));
        when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));
        when(editorRepository.findBySheetIdAndUserId(sheet.getId(), requester)).thenReturn(Optional.of(editor));
        when(editorRepository.save(editor)).thenReturn(editor);

        CharacterSheetEditRequestResponse response = service.requestEdit(saved.getId());

        assertEquals(CharacterSheetEditorStatus.APPROVED, response.getStatus());
    }

    @Test
    void requestRightAfterDeclineIsRejected() {
        UUID requester = authenticate();
        CharacterSheet sheet = sharedSheet(UUID.randomUUID());
        SavedCharacterSheet saved = savedRecord(requester, sheet);
        CharacterSheetEditor editor = editor(sheet, requester, CharacterSheetEditorStatus.DECLINED);
        editor.setDecidedAt(Instant.now().minus(Duration.ofHours(1)));
        when(savedRepository.findByIdAndUserId(saved.getId(), requester)).thenReturn(Optional.of(saved));
        when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));
        when(editorRepository.findBySheetIdAndUserId(sheet.getId(), requester)).thenReturn(Optional.of(editor));

        ApiException exception = assertThrows(ApiException.class, () -> service.requestEdit(saved.getId()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(CharacterSheetEditorStatus.DECLINED, editor.getStatus());
    }

    @Test
    void requestAfterCooldownIsPendingAgain() {
        UUID requester = authenticate();
        CharacterSheet sheet = sharedSheet(UUID.randomUUID());
        SavedCharacterSheet saved = savedRecord(requester, sheet);
        CharacterSheetEditor editor = editor(sheet, requester, CharacterSheetEditorStatus.DECLINED);
        editor.setDecidedAt(Instant.now().minus(Duration.ofDays(2)));
        when(savedRepository.findByIdAndUserId(saved.getId(), requester)).thenReturn(Optional.of(saved));
        when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));
        when(editorRepository.findBySheetIdAndUserId(sheet.getId(), requester)).thenReturn(Optional.of(editor));
        when(editorRepository.save(editor)).thenReturn(editor);

        CharacterSheetEditRequestResponse response = service.requestEdit(saved.getId());

        assertEquals(CharacterSheetEditorStatus.PENDING, response.getStatus());
        assertNull(editor.getDecidedAt());
    }

    @Test
    void ownerApprovesRequestAndSeesNameInsteadOfLogin() {
        UUID owner = authenticate();
        CharacterSheet sheet = sharedSheet(owner);
        CharacterSheetEditor editor = editor(sheet, UUID.randomUUID(), CharacterSheetEditorStatus.PENDING);
        when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));
        when(editorRepository.findByIdAndSheetId(editor.getId(), sheet.getId())).thenReturn(Optional.of(editor));
        when(editorRepository.findAllBySheetIdAndStatusInOrderByCreatedAtAsc(any(), anyCollection()))
                .thenReturn(List.of(editor));
        when(displayNameService.resolveByUserIds(anyCollection())).thenReturn(
                List.of(new DisplayNameByUserIdResponse(editor.getUserId(), "Мастер Игорь", null)));

        CharacterSheetEditorListResponse response = service.approve(sheet.getId(), editor.getId());

        assertEquals(CharacterSheetEditorStatus.APPROVED, editor.getStatus());
        assertNotNull(editor.getDecidedAt());
        assertEquals("Мастер Игорь", response.getEditors().getFirst().getDisplayName());
    }

    @Test
    void approveOverEditorLimitIsRejected() {
        UUID owner = authenticate();
        CharacterSheet sheet = sharedSheet(owner);
        CharacterSheetEditor editor = editor(sheet, UUID.randomUUID(), CharacterSheetEditorStatus.PENDING);
        when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));
        when(editorRepository.findByIdAndSheetId(editor.getId(), sheet.getId())).thenReturn(Optional.of(editor));
        when(editorRepository.countBySheetIdAndStatus(sheet.getId(), CharacterSheetEditorStatus.APPROVED))
                .thenReturn((long) CharacterSheetEditorService.MAX_EDITORS);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.approve(sheet.getId(), editor.getId()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(CharacterSheetEditorStatus.PENDING, editor.getStatus());
    }

    @Test
    void editorCannotApproveOthers() {
        authenticate();
        CharacterSheet sheet = sharedSheet(UUID.randomUUID());
        when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.approve(sheet.getId(), UUID.randomUUID()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void removeRevokesRightAndStartsCooldown() {
        UUID owner = authenticate();
        CharacterSheet sheet = sharedSheet(owner);
        CharacterSheetEditor editor = editor(sheet, UUID.randomUUID(), CharacterSheetEditorStatus.APPROVED);
        when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));
        when(editorRepository.findByIdAndSheetId(editor.getId(), sheet.getId())).thenReturn(Optional.of(editor));

        service.remove(sheet.getId(), editor.getId());

        assertEquals(CharacterSheetEditorStatus.DECLINED, editor.getStatus());
        assertNotNull(editor.getDecidedAt());
    }

    @Test
    void countIncomingCountsOwnersPendingRequests() {
        UUID owner = authenticate();
        when(editorRepository.countPendingForOwner(owner)).thenReturn(3L);

        assertEquals(3L, service.countIncoming().getCount());
    }

    @Test
    void editAccessListsOnlyRequestedSavedSheets() {
        UUID requester = authenticate();
        CharacterSheet requested = sharedSheet(UUID.randomUUID());
        CharacterSheet untouched = sharedSheet(UUID.randomUUID());
        SavedCharacterSheet requestedSaved = savedRecord(requester, requested);
        SavedCharacterSheet untouchedSaved = savedRecord(requester, untouched);
        when(savedRepository.findAllByUserIdOrderByCreatedAtDesc(requester))
                .thenReturn(List.of(requestedSaved, untouchedSaved));
        when(editorRepository.findAllByUserIdAndSheetIdIn(requester, List.of(requested.getId(), untouched.getId())))
                .thenReturn(List.of(editor(requested, requester, CharacterSheetEditorStatus.APPROVED)));

        List<CharacterSheetEditAccessResponse> sheets = service.findMyEditAccess().getSheets();

        assertEquals(1, sheets.size());
        assertEquals(requestedSaved.getId(), sheets.getFirst().getSavedId());
        assertEquals(CharacterSheetEditorStatus.APPROVED, sheets.getFirst().getStatus());
    }

    @Test
    void editAccessWithoutSavedSheetsIsEmpty() {
        UUID requester = authenticate();
        when(savedRepository.findAllByUserIdOrderByCreatedAtDesc(requester)).thenReturn(List.of());

        assertEquals(0, service.findMyEditAccess().getSheets().size());
        verify(editorRepository, never()).findAllByUserIdAndSheetIdIn(any(), anyCollection());
    }

    private static CharacterSheet sharedSheet(UUID ownerId) {
        CharacterSheet sheet = new CharacterSheet();
        sheet.setId(UUID.randomUUID());
        sheet.setUserId(ownerId);
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

    private static CharacterSheetEditor editor(CharacterSheet sheet, UUID userId, CharacterSheetEditorStatus status) {
        CharacterSheetEditor editor = new CharacterSheetEditor();
        editor.setId(UUID.randomUUID());
        editor.setSheetId(sheet.getId());
        editor.setUserId(userId);
        editor.setStatus(status);
        return editor;
    }

    private static UUID authenticate() {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
        return user.getUuid();
    }
}
