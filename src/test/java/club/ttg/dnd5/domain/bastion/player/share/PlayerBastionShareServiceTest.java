package club.ttg.dnd5.domain.bastion.player.share;

import club.ttg.dnd5.domain.bastion.service.FacilityChoiceOptions;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionEditor;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionMember;
import club.ttg.dnd5.domain.bastion.player.plan.PlayerBastionPlanService;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionEditorRepository;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionRepository;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionResponse;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionAccess;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionResponseMapper;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.domain.user.service.UserService;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerBastionShareServiceTest {
    private final PlayerBastionRepository bastionRepository = mock(PlayerBastionRepository.class);
    private final PlayerBastionEditorRepository editorRepository = mock(PlayerBastionEditorRepository.class);
    private final UserService userService = mock(UserService.class);
    private final PlayerBastionAccess access = new PlayerBastionAccess(bastionRepository, editorRepository,
            mock(GameMembershipClient.class), userService);
    private final PlayerBastionResponseMapper mapper = new PlayerBastionResponseMapper(
            mock(BastionFacilityRepository.class), editorRepository, new FacilityChoiceOptions(mock(ItemRepository.class)));
    private final PlayerBastionShareService service = new PlayerBastionShareService(bastionRepository,
            editorRepository, access, mapper, mock(PlayerBastionPlanService.class), userService);

    private final UUID ownerId = UUID.randomUUID();
    private final UUID friendId = UUID.randomUUID();
    private final List<PlayerBastionEditor> editors = new ArrayList<>();

    private PlayerBastion bastion;

    @BeforeEach
    void setUp() {
        bastion = new PlayerBastion();
        bastion.setId(UUID.randomUUID());
        bastion.setOwnerId(ownerId);
        bastion.setName("Вороний утёс");
        PlayerBastionMember member = new PlayerBastionMember();
        member.setBastion(bastion);
        member.setUserId(ownerId);
        member.setCharacterName("Дриззт");
        member.setCharacterLevel(5);
        bastion.getMembers().add(member);

        when(bastionRepository.findWithMembersById(bastion.getId())).thenReturn(Optional.of(bastion));
        when(bastionRepository.save(any(PlayerBastion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bastionRepository.findWithMembersByShareToken(any())).thenAnswer(invocation ->
                Optional.ofNullable(invocation.getArgument(0)).filter(token -> token.equals(bastion.getShareToken()))
                        .map(token -> bastion));

        when(editorRepository.save(any(PlayerBastionEditor.class))).thenAnswer(invocation -> {
            PlayerBastionEditor editor = invocation.getArgument(0);
            if (editor.getId() == null) {
                editor.setId(UUID.randomUUID());
                editors.add(editor);
            }
            return editor;
        });
        when(editorRepository.findById(any())).thenAnswer(invocation -> editors.stream()
                .filter(editor -> editor.getId().equals(invocation.getArgument(0)))
                .findFirst());
        when(editorRepository.findByBastionIdAndUserId(any(), any())).thenAnswer(invocation -> editors.stream()
                .filter(editor -> editor.getBastionId().equals(invocation.getArgument(0))
                        && editor.getUserId().equals(invocation.getArgument(1)))
                .findFirst());
        when(editorRepository.existsByBastionIdAndUserIdAndStatus(any(), any(), any())).thenAnswer(invocation ->
                editors.stream().anyMatch(editor -> editor.getBastionId().equals(invocation.getArgument(0))
                        && editor.getUserId().equals(invocation.getArgument(1))
                        && editor.getStatus() == invocation.getArgument(2)));
        when(editorRepository.findAllByBastionIdOrderByCreatedAtAsc(any())).thenAnswer(invocation -> editors.stream()
                .filter(editor -> editor.getBastionId().equals(invocation.getArgument(0)))
                .toList());
        when(editorRepository.countByBastionIdAndStatus(eq(bastion.getId()), any())).thenAnswer(invocation ->
                editors.stream().filter(editor -> editor.getStatus() == invocation.getArgument(1)).count());
        doAnswer(invocation -> editors.remove(invocation.<PlayerBastionEditor>getArgument(0)))
                .when(editorRepository).delete(any(PlayerBastionEditor.class));
        doAnswer(invocation -> {
            editors.clear();
            return null;
        }).when(editorRepository).deleteAllByBastionId(any());
    }

    @Test
    void onlyOwnerSharesAndTokenIsStable() {
        actAs(friendId);
        assertEquals(HttpStatus.FORBIDDEN,
                assertThrows(ApiException.class, () -> service.share(bastion.getId())).getStatus());

        actAs(ownerId);
        UUID token = service.share(bastion.getId()).shareToken();

        assertNotNull(token);
        assertEquals(token, service.share(bastion.getId()).shareToken());
    }

    @Test
    void linkOpensReadOnlyForAnonymous() {
        actAs(ownerId);
        String token = service.share(bastion.getId()).shareToken().toString();
        when(userService.getCurrentUserId()).thenReturn(Optional.empty());

        SharedPlayerBastionResponse shared = service.findShared(token);

        assertEquals(SharedPlayerBastionResponse.Access.ANONYMOUS, shared.access());
        assertFalse(shared.bastion().canEdit());
        assertFalse(shared.bastion().canManage());
        assertNull(shared.bastion().shareToken());
        assertThrows(EntityNotFoundException.class, () -> service.findShared("не-токен"));
        assertThrows(EntityNotFoundException.class, () -> service.findShared(UUID.randomUUID().toString()));
    }

    @Test
    void approvedRequestGivesEditRightsAndRevokeTakesThemBack() {
        actAs(ownerId);
        String token = service.share(bastion.getId()).shareToken().toString();

        actAs(friendId);
        assertEquals(SharedPlayerBastionResponse.Access.NONE, service.findShared(token).access());
        assertEquals(SharedPlayerBastionResponse.Access.PENDING, service.requestEdit(token));
        assertEquals(SharedPlayerBastionResponse.Access.PENDING, service.requestEdit(token));
        assertEquals(1, editors.size());

        actAs(ownerId);
        assertEquals(1, service.share(bastion.getId()).editRequests());
        service.approve(bastion.getId(), editors.getFirst().getId());

        actAs(friendId);
        assertEquals(SharedPlayerBastionResponse.Access.GRANTED, service.findShared(token).access());
        PlayerBastionResponse opened = mapper.toResponse(bastion, access.requireViewer(bastion, friendId));
        assertTrue(opened.canEdit());
        assertTrue(opened.editor());
        assertFalse(opened.owned());

        actAs(ownerId);
        service.revoke(bastion.getId());

        assertTrue(editors.isEmpty());
        assertNull(bastion.getShareToken());
        assertThrows(EntityNotFoundException.class, () -> service.findShared(token));
    }

    @Test
    void ownerDeclinesRequest() {
        actAs(ownerId);
        String token = service.share(bastion.getId()).shareToken().toString();
        actAs(friendId);
        service.requestEdit(token);

        actAs(friendId);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ApiException.class,
                () -> service.approve(bastion.getId(), editors.getFirst().getId())).getStatus());

        actAs(ownerId);
        assertTrue(service.remove(bastion.getId(), editors.getFirst().getId()).isEmpty());
        actAs(friendId);
        assertEquals(SharedPlayerBastionResponse.Access.NONE, service.findShared(token).access());
    }

    private void actAs(UUID userId) {
        when(userService.getCurrentUserId()).thenReturn(Optional.of(userId));
    }
}
