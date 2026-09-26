package club.ttg.dnd5.domain.bastion.player.service;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilityPrerequisite;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembership;
import club.ttg.dnd5.domain.bastion.player.client.GameRole;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionFacility;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionMember;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionRepository;
import club.ttg.dnd5.domain.bastion.player.rest.dto.FacilitySetupRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionResponse;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.domain.user.service.UserService;
import club.ttg.dnd5.exception.ApiException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerBastionFacilityServiceTest {
    private final PlayerBastionRepository repository = mock(PlayerBastionRepository.class);
    private final BastionFacilityRepository facilityRepository = mock(BastionFacilityRepository.class);
    private final GameMembershipClient client = mock(GameMembershipClient.class);
    private final UserService userService = mock(UserService.class);
    private final PlayerBastionFacilityService service = new PlayerBastionFacilityService(
            repository, facilityRepository,
            new PlayerBastionAccess(repository, client, userService),
            new PlayerBastionResponseMapper(facilityRepository),
            mock(EntityManager.class));

    private final UUID gameId = UUID.randomUUID();
    private final UUID masterId = UUID.randomUUID();
    private final UUID playerId = UUID.randomUUID();
    private final UUID otherPlayerId = UUID.randomUUID();
    private final Map<String, BastionFacility> reference = Map.of(
            "bedroom", facility("bedroom", FacilityCategory.BASIC, null),
            "sanctuary", facility("sanctuary", FacilityCategory.SPECIAL, FacilityPrerequisite.HOLY_SYMBOL_OR_DRUIDIC_FOCUS),
            "library", facility("library", FacilityCategory.SPECIAL, null));

    private PlayerBastion bastion;
    private PlayerBastionMember member;

    @BeforeEach
    void setUp() {
        when(client.membership(gameId, masterId)).thenReturn(membership(masterId, GameRole.MASTER));
        when(client.membership(gameId, playerId)).thenReturn(membership(playerId, GameRole.PLAYER));
        when(client.membership(gameId, otherPlayerId)).thenReturn(membership(otherPlayerId, GameRole.PLAYER));
        when(repository.saveAndFlush(any(PlayerBastion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(facilityRepository.findAllById(anyCollection())).thenAnswer(invocation -> {
            Collection<String> urls = invocation.getArgument(0);
            return urls.stream().map(reference::get).filter(java.util.Objects::nonNull).toList();
        });

        bastion = new PlayerBastion();
        bastion.setId(UUID.randomUUID());
        bastion.setGameId(gameId);
        bastion.setMasterId(masterId);
        bastion.setName("Башня");
        member = new PlayerBastionMember();
        member.setId(UUID.randomUUID());
        member.setBastion(bastion);
        member.setUserId(playerId);
        member.setCharacterName("Дриззт");
        member.setCharacterLevel(5);
        bastion.getMembers().add(member);
        when(repository.findWithMembersById(bastion.getId())).thenReturn(Optional.of(bastion));
    }

    @Test
    void playerChoosesFacilitiesOfOwnCharacter() {
        actAs(playerId);

        PlayerBastionResponse response = service.updateSetup(bastion.getId(), member.getId(), request(
                List.of(new FacilitySetupRequest.Basic("bedroom", FacilitySpace.CRAMPED)),
                List.of(new FacilitySetupRequest.Special("library", List.of()))));

        List<PlayerBastionResponse.Facility> facilities = response.members().getFirst().facilities();
        assertEquals(2, facilities.size());
        assertEquals("CRAMPED", facilities.getFirst().space().value());
        assertTrue(facilities.get(1).prerequisiteConfirmed());
        assertFalse(response.members().getFirst().basicComplete());
    }

    @Test
    void anotherPlayerCannotChooseForCharacter() {
        actAs(otherPlayerId);

        ApiException error = assertThrows(ApiException.class,
                () -> service.updateSetup(bastion.getId(), member.getId(), request(List.of(), List.of())));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    }

    @Test
    void startingSelectionIsClosedAfterSetup() {
        bastion.setStatus(PlayerBastionStatus.ACTIVE);
        actAs(masterId);

        ApiException error = assertThrows(ApiException.class,
                () -> service.updateSetup(bastion.getId(), member.getId(), request(List.of(), List.of())));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    /** Требование ждёт мастера; подтверждение не теряется при пересохранении выбора. */
    @Test
    void prerequisiteNeedsMasterAndSurvivesResave() {
        actAs(playerId);
        FacilitySetupRequest withSanctuary = request(List.of(),
                List.of(new FacilitySetupRequest.Special("sanctuary", List.of())));
        service.updateSetup(bastion.getId(), member.getId(), withSanctuary);
        PlayerBastionFacility sanctuary = member.getFacilities().getFirst();
        assertFalse(sanctuary.isPrerequisiteConfirmed());

        sanctuary.setId(UUID.randomUUID());
        actAs(masterId);
        service.confirmPrerequisite(bastion.getId(), sanctuary.getId(), true);
        actAs(playerId);
        service.updateSetup(bastion.getId(), member.getId(), withSanctuary);

        assertTrue(member.getFacilities().getFirst() == sanctuary);
        assertTrue(sanctuary.isPrerequisiteConfirmed());
    }

    @Test
    void playerCannotConfirmPrerequisite() {
        actAs(playerId);

        ApiException error = assertThrows(ApiException.class,
                () -> service.confirmPrerequisite(bastion.getId(), UUID.randomUUID(), true));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    }

    @Test
    void levelCannotDropBelowChosenFacilities() {
        actAs(playerId);
        service.updateSetup(bastion.getId(), member.getId(), request(List.of(),
                List.of(new FacilitySetupRequest.Special("library", List.of()))));

        ApiException error = assertThrows(ApiException.class, () -> service.requireLevelFits(member, 4));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    private FacilitySetupRequest request(List<FacilitySetupRequest.Basic> basic,
                                         List<FacilitySetupRequest.Special> special) {
        return new FacilitySetupRequest(basic, special, bastion.getVersion());
    }

    private void actAs(UUID userId) {
        when(userService.getCurrentUserId()).thenReturn(Optional.of(userId));
    }

    private GameMembership membership(UUID userId, GameRole role) {
        return new GameMembership(gameId, "Проклятие Страда", masterId, userId, role);
    }

    private static BastionFacility facility(String url, FacilityCategory category, FacilityPrerequisite prerequisite) {
        BastionFacility facility = new BastionFacility();
        facility.setUrl(url);
        facility.setName(url);
        facility.setCategory(category);
        facility.setPrerequisite(prerequisite);
        if (category == FacilityCategory.SPECIAL) {
            facility.setLevel(5L);
            facility.setSpace(FacilitySpace.ROOMY);
        }
        return facility;
    }
}
