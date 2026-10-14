package club.ttg.dnd5.domain.bastion.player.service;

import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembers;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembership;
import club.ttg.dnd5.domain.bastion.player.client.GameRole;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionMember;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionRepository;
import club.ttg.dnd5.domain.bastion.player.rest.dto.CreatePlayerBastionRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.JoinGameRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionGameResponse;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionMemberRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionResponse;
import club.ttg.dnd5.domain.bastion.player.rest.dto.UpdatePlayerBastionRequest;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.domain.user.service.UserService;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

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

class PlayerBastionServiceTest {
    private final PlayerBastionRepository repository = mock(PlayerBastionRepository.class);
    private final GameMembershipClient client = mock(GameMembershipClient.class);
    private final UserService userService = mock(UserService.class);
    private final BastionFacilityRepository facilityRepository = mock(BastionFacilityRepository.class);
    private final PlayerBastionFacilityService facilityService = mock(PlayerBastionFacilityService.class);
    private final PlayerBastionLimits limits = mock(PlayerBastionLimits.class);
    private final PlayerBastionService service = new PlayerBastionService(repository, client,
            new PlayerBastionAccess(repository, client, userService),
            new PlayerBastionResponseMapper(facilityRepository), facilityService, limits);

    private final UUID gameId = UUID.randomUUID();
    private final UUID masterId = UUID.randomUUID();
    private final UUID playerId = UUID.randomUUID();
    private final UUID strangerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(client.membership(gameId, masterId)).thenReturn(membership(masterId, GameRole.MASTER));
        when(client.membership(gameId, playerId)).thenReturn(membership(playerId, GameRole.PLAYER));
        when(client.membership(gameId, strangerId)).thenReturn(membership(strangerId, GameRole.NONE));
        when(client.members(gameId)).thenReturn(new GameMembers(gameId, "Проклятие Страда", masterId,
                List.of(new GameMembers.Player(playerId, "Дриззт"))));
        when(repository.save(any(PlayerBastion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveAndFlush(any(PlayerBastion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(limits.forCurrentUser()).thenReturn(PlayerBastionLimits.BASE);
    }

    /** Бастион принадлежит создавшему, его персонаж сразу в составе, бастион вне игры. */
    @Test
    void anyoneCreatesOwnBastionOutsideGame() {
        actAs(playerId);
        when(repository.countByOwnerId(playerId)).thenReturn(0L);

        PlayerBastionResponse response = service.create(
                new CreatePlayerBastionRequest(" Вороний утёс ", "Дриззт", 9, null));

        assertEquals("Вороний утёс", response.name());
        assertEquals(PlayerBastionStatus.SETUP, response.status());
        assertNull(response.gameId());
        assertTrue(response.owned());
        assertTrue(response.canManage());
        PlayerBastionResponse.Member member = response.members().getFirst();
        assertEquals(playerId, member.userId());
        assertEquals("Дриззт", member.characterName());
        assertEquals(4, member.specialFacilityLimit());
    }

    @Test
    void creationStopsAtLimit() {
        actAs(playerId);
        when(repository.countByOwnerId(playerId)).thenReturn((long) PlayerBastionLimits.BASE);

        ApiException error = assertThrows(ApiException.class,
                () -> service.create(new CreatePlayerBastionRequest("Башня", null, 5, null)));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertTrue(error.getMessage().contains("3"), error.getMessage());
        verify(repository, never()).save(any());
    }

    /** Вне игры дать доступ другим нельзя: брать игроков неоткуда. */
    @Test
    void outsideGameOnlyOwnerIsInComposition() {
        PlayerBastion bastion = ownBastion(playerId);
        when(repository.findWithMembersById(bastion.getId())).thenReturn(Optional.of(bastion));
        actAs(playerId);

        ApiException error = assertThrows(ApiException.class, () -> service.update(bastion.getId(),
                new UpdatePlayerBastionRequest("Башня", List.of(
                        new PlayerBastionMemberRequest(playerId, "Дриззт", 5, null),
                        new PlayerBastionMemberRequest(strangerId, "Чужак", 5, null)), 0L)));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    /** В игре доступ даётся только игрокам игры с одобренной заявкой. */
    @Test
    void inGameAccessCanBeGrantedOnlyToApprovedPlayers() {
        PlayerBastion bastion = bastion("Башня", playerId);
        when(repository.findWithMembersById(bastion.getId())).thenReturn(Optional.of(bastion));
        actAs(masterId);

        ApiException error = assertThrows(ApiException.class, () -> service.update(bastion.getId(),
                new UpdatePlayerBastionRequest("Башня", List.of(
                        new PlayerBastionMemberRequest(playerId, null, 5, null),
                        new PlayerBastionMemberRequest(strangerId, "Чужак", 5, null)), 0L)));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    /** Игрок добавляет свой бастион в игру, и вести его начинает мастер этой игры. */
    @Test
    void ownerAddsBastionToGameAndMasterTakesOver() {
        PlayerBastion bastion = ownBastion(playerId);
        when(repository.findWithMembersById(bastion.getId())).thenReturn(Optional.of(bastion));
        actAs(playerId);

        PlayerBastionResponse response = service.joinGame(bastion.getId(), new JoinGameRequest(gameId));

        assertEquals(gameId, response.gameId());
        assertEquals(masterId, bastion.getMasterId());
        assertFalse(response.canManage());
        assertTrue(response.canEdit());
        assertTrue(response.canLeaveGame());
    }

    @Test
    void strangerCannotAddBastionToGame() {
        PlayerBastion bastion = ownBastion(strangerId);
        when(repository.findWithMembersById(bastion.getId())).thenReturn(Optional.of(bastion));
        actAs(strangerId);

        ApiException error = assertThrows(ApiException.class,
                () -> service.joinGame(bastion.getId(), new JoinGameRequest(gameId)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        assertNull(bastion.getGameId());
    }

    @Test
    void bastionCannotBeInTwoGames() {
        PlayerBastion bastion = bastion("Башня", playerId);
        bastion.setOwnerId(playerId);
        when(repository.findWithMembersById(bastion.getId())).thenReturn(Optional.of(bastion));
        actAs(playerId);

        ApiException error = assertThrows(ApiException.class,
                () -> service.joinGame(bastion.getId(), new JoinGameRequest(UUID.randomUUID())));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    /** Мастер убирает чужой бастион из своей игры, и тот возвращается к владельцу. */
    @Test
    void masterRemovesBastionFromGame() {
        PlayerBastion bastion = bastion("Башня", playerId);
        bastion.setOwnerId(playerId);
        when(repository.findWithMembersById(bastion.getId())).thenReturn(Optional.of(bastion));
        actAs(masterId);

        service.leaveGame(bastion.getId());

        assertNull(bastion.getGameId());
        assertNull(bastion.getMasterId());
    }

    @Test
    void onlyOwnerDeletesBastion() {
        PlayerBastion bastion = bastion("Башня", playerId);
        bastion.setOwnerId(playerId);
        when(repository.findWithMembersById(bastion.getId())).thenReturn(Optional.of(bastion));
        actAs(masterId);

        ApiException error = assertThrows(ApiException.class, () -> service.delete(bastion.getId()));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        verify(repository, never()).deleteBastion(any());

        actAs(playerId);
        service.delete(bastion.getId());

        verify(repository).deleteBastion(bastion.getId());
    }

    /** Смотреть бастионы игры может любой участник, менять — только те, у кого есть доступ. */
    @Test
    void participantSeesAllBastionsButEditsOnlyOwn() {
        PlayerBastion own = bastion("Свой", playerId);
        PlayerBastion other = bastion("Чужой", UUID.randomUUID());
        when(repository.findAllByGameIdOrderByCreatedAtAsc(gameId)).thenReturn(List.of(own, other));
        actAs(playerId);

        PlayerBastionGameResponse response = service.findForGame(gameId);

        assertEquals(2, response.bastions().size());
        assertTrue(response.canAdd());
        assertTrue(response.players().isEmpty());
        assertTrue(response.bastions().get(0).canEdit());
        assertFalse(response.bastions().get(1).canEdit());
        assertFalse(response.bastions().get(0).canManage());
    }

    @Test
    void strangerDoesNotSeeBastions() {
        actAs(strangerId);

        ApiException error = assertThrows(ApiException.class, () -> service.findForGame(gameId));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    }

    @Test
    void outdatedVersionIsRejected() {
        PlayerBastion bastion = bastion("Башня", playerId);
        bastion.setVersion(3);
        when(repository.findWithMembersById(bastion.getId())).thenReturn(Optional.of(bastion));
        actAs(masterId);

        ApiException error = assertThrows(ApiException.class, () -> service.update(bastion.getId(),
                new UpdatePlayerBastionRequest("Башня", List.of(), 2L)));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    /** Правка состава обновляет существующего участника на месте, а не пересоздаёт его. */
    @Test
    void updateKeepsExistingMemberRecord() {
        PlayerBastion bastion = bastion("Башня", playerId);
        PlayerBastionMember existing = bastion.getMembers().getFirst();
        when(repository.findWithMembersById(bastion.getId())).thenReturn(Optional.of(bastion));
        actAs(masterId);

        service.update(bastion.getId(), new UpdatePlayerBastionRequest("Башня",
                List.of(new PlayerBastionMemberRequest(playerId, "Дриззт До'Урден", 13, null)), 0L));

        assertEquals(1, bastion.getMembers().size());
        assertTrue(bastion.getMembers().getFirst() == existing);
        assertEquals(13, existing.getCharacterLevel());
        assertEquals("Дриззт До'Урден", existing.getCharacterName());
    }

    @Test
    void archivedBastionIsReadOnly() {
        PlayerBastion bastion = bastion("Башня", playerId);
        bastion.setStatus(PlayerBastionStatus.ARCHIVED);
        when(repository.findWithMembersById(bastion.getId())).thenReturn(Optional.of(bastion));
        actAs(masterId);

        PlayerBastionResponse response = service.findById(bastion.getId());

        assertFalse(response.canManage());
        assertFalse(response.canEdit());
    }

    private void actAs(UUID userId) {
        when(userService.getCurrentUserId()).thenReturn(Optional.of(userId));
    }

    private GameMembership membership(UUID userId, GameRole role) {
        return new GameMembership(gameId, "Проклятие Страда", masterId, userId, role);
    }

    /** Свой бастион вне игры: владелец — единственный в составе. */
    private PlayerBastion ownBastion(UUID ownerId) {
        PlayerBastion bastion = bastion("Башня", ownerId);
        bastion.setOwnerId(ownerId);
        bastion.setGameId(null);
        bastion.setMasterId(null);
        return bastion;
    }

    private PlayerBastion bastion(String name, UUID memberUserId) {
        PlayerBastion bastion = new PlayerBastion();
        bastion.setId(UUID.randomUUID());
        bastion.setGameId(gameId);
        bastion.setMasterId(masterId);
        bastion.setOwnerId(masterId);
        bastion.setName(name);
        PlayerBastionMember member = new PlayerBastionMember();
        member.setBastion(bastion);
        member.setUserId(memberUserId);
        member.setCharacterName("Персонаж");
        member.setCharacterLevel(5);
        bastion.getMembers().add(member);
        return bastion;
    }
}
