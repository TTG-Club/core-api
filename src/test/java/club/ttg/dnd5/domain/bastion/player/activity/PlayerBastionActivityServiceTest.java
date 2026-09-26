package club.ttg.dnd5.domain.bastion.player.activity;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilityOrderOption;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.activity.PlayerBastionOrder.OrderStatus;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembership;
import club.ttg.dnd5.domain.bastion.player.client.GameRole;
import club.ttg.dnd5.domain.bastion.player.model.FacilityStatus;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionFacility;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionMember;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionRepository;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionAccess;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionResponseMapper;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.domain.user.service.UserService;
import club.ttg.dnd5.exception.ApiException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerBastionActivityServiceTest {
    private final PlayerBastionRepository bastionRepository = mock(PlayerBastionRepository.class);
    private final BastionFacilityRepository facilityRepository = mock(BastionFacilityRepository.class);
    private final PlayerBastionOrderRepository orderRepository = mock(PlayerBastionOrderRepository.class);
    private final PlayerBastionLedgerRepository ledgerRepository = mock(PlayerBastionLedgerRepository.class);
    private final PlayerBastionTurnRepository turnRepository = mock(PlayerBastionTurnRepository.class);
    private final GameMembershipClient client = mock(GameMembershipClient.class);
    private final UserService userService = mock(UserService.class);
    private final PlayerBastionActivityService service = new PlayerBastionActivityService(
            bastionRepository, facilityRepository, orderRepository, ledgerRepository, turnRepository,
            new PlayerBastionAccess(bastionRepository, client, userService),
            new PlayerBastionResponseMapper(facilityRepository),
            mock(EntityManager.class));

    private final UUID gameId = UUID.randomUUID();
    private final UUID masterId = UUID.randomUUID();
    private final UUID playerId = UUID.randomUUID();
    private final UUID otherPlayerId = UUID.randomUUID();

    private final List<PlayerBastionOrder> orders = new ArrayList<>();
    private final List<PlayerBastionLedgerEntry> ledger = new ArrayList<>();
    private final List<PlayerBastionTurn> turns = new ArrayList<>();
    private final Map<String, BastionFacility> reference = Map.of(
            "library", library(),
            "bedroom", basic("bedroom"));

    private PlayerBastion bastion;
    private PlayerBastionMember member;
    private PlayerBastionFacility library;

    @BeforeEach
    void setUp() {
        when(client.membership(gameId, masterId)).thenReturn(membership(masterId, GameRole.MASTER));
        when(client.membership(gameId, playerId)).thenReturn(membership(playerId, GameRole.PLAYER));
        when(client.membership(gameId, otherPlayerId)).thenReturn(membership(otherPlayerId, GameRole.PLAYER));
        when(bastionRepository.saveAndFlush(any(PlayerBastion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(facilityRepository.findAllById(anyCollection())).thenAnswer(invocation -> {
            Collection<String> urls = invocation.getArgument(0);
            return urls.stream().map(reference::get).filter(Objects::nonNull).toList();
        });
        when(facilityRepository.findById(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(reference.get(invocation.<String>getArgument(0))));

        when(orderRepository.save(any(PlayerBastionOrder.class))).thenAnswer(invocation -> {
            PlayerBastionOrder order = invocation.getArgument(0);
            if (order.getId() == null) {
                order.setId(UUID.randomUUID());
                orders.add(order);
            }
            return order;
        });
        when(orderRepository.findAllByBastionIdAndStatus(any(), any())).thenAnswer(invocation -> orders.stream()
                .filter(order -> order.getStatus() == invocation.getArgument(1))
                .toList());
        when(orderRepository.findAllByBastionIdAndGivenOnTurnAndStatusNot(any(), anyInt(), any()))
                .thenAnswer(invocation -> orders.stream()
                        .filter(order -> order.getGivenOnTurn() == invocation.<Integer>getArgument(1)
                                && order.getStatus() != invocation.getArgument(2))
                        .toList());
        when(orderRepository.findById(any())).thenAnswer(invocation -> orders.stream()
                .filter(order -> order.getId().equals(invocation.getArgument(0)))
                .findFirst());
        when(ledgerRepository.save(any())).thenAnswer(invocation -> {
            ledger.add(invocation.getArgument(0));
            return invocation.getArgument(0);
        });
        when(turnRepository.save(any())).thenAnswer(invocation -> {
            turns.add(invocation.getArgument(0));
            return invocation.getArgument(0);
        });

        bastion = new PlayerBastion();
        bastion.setId(UUID.randomUUID());
        bastion.setGameId(gameId);
        bastion.setMasterId(masterId);
        bastion.setName("Вороний утёс");
        bastion.setStatus(PlayerBastionStatus.ACTIVE);
        bastion.setTreasuryGp(100);
        member = new PlayerBastionMember();
        member.setId(UUID.randomUUID());
        member.setBastion(bastion);
        member.setUserId(playerId);
        member.setCharacterName("Дриззт");
        member.setCharacterLevel(5);
        bastion.getMembers().add(member);
        library = new PlayerBastionFacility();
        library.setId(UUID.randomUUID());
        library.setMember(member);
        library.setFacilityUrl("library");
        library.setSpace(FacilitySpace.ROOMY);
        library.setPrerequisiteConfirmed(true);
        member.getFacilities().add(library);
        when(bastionRepository.findWithMembersById(bastion.getId())).thenReturn(Optional.of(bastion));
    }

    @Test
    void turnsRoundUpDays() {
        assertEquals(1, PlayerBastionActivityService.turnsFor(null));
        assertEquals(1, PlayerBastionActivityService.turnsFor(7));
        assertEquals(3, PlayerBastionActivityService.turnsFor(20));
        assertEquals(18, PlayerBastionActivityService.turnsFor(125));
    }

    @Test
    void orderWithFixedCostIsChargedAndCompletedOnTurn() {
        actAs(playerId);
        service.giveOrder(bastion.getId(), new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH,
                "тему", "Легенда о Страде"));

        assertEquals(90, bastion.getTreasuryGp());
        assertEquals(-10, ledger.getFirst().getAmountGp());

        actAs(masterId);
        service.performTurn(bastion.getId(), new ActivityRequests.Turn(null,
                Map.of(orders.getFirst().getId(), "Три факта о Страде")));

        PlayerBastionOrder order = orders.getFirst();
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals("Три факта о Страде", order.getResult());
        assertEquals(1, bastion.getTurn());
        assertEquals(false, turns.getFirst().isMaintain());
    }

    @Test
    void notEnoughGoldIsRejectedWithoutCharging() {
        bastion.setTreasuryGp(5);
        actAs(playerId);

        ApiException error = assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH, "тему", null)));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals(5, bastion.getTreasuryGp());
        assertTrue(orders.isEmpty());
    }

    @Test
    void anotherPlayerCannotOrderForeignFacility() {
        actAs(otherPlayerId);

        ApiException error = assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH, null, null)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    }

    @Test
    void facilityAcceptsOnlyItsOrdersAndOneAtATime() {
        actAs(playerId);
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(library.getId(), BastionOrder.CRAFT, null, null))).getStatus());

        service.giveOrder(bastion.getId(), new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH, null, null));

        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH, null, null))).getStatus());
    }

    /** «Обслуживать» отдаётся вместо остальных приказов хода, и наоборот. */
    @Test
    void maintainExcludesOtherOrdersInTurn() {
        actAs(playerId);
        service.giveOrder(bastion.getId(), new ActivityRequests.GiveOrder(null, BastionOrder.MAINTAIN, null, null));

        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH, null, null))).getStatus());

        actAs(masterId);
        service.performTurn(bastion.getId(), new ActivityRequests.Turn("Гость из соседнего королевства", Map.of()));

        assertTrue(turns.getFirst().isMaintain());
        assertEquals("Гость из соседнего королевства", turns.getFirst().getEvent());
    }

    @Test
    void turnWithoutOrdersIsMaintenance() {
        actAs(masterId);

        service.performTurn(bastion.getId(), new ActivityRequests.Turn(null, null));

        assertTrue(turns.getFirst().isMaintain());
    }

    @Test
    void cancelInSameTurnRefunds() {
        actAs(playerId);
        service.giveOrder(bastion.getId(), new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH,
                "тему", null));

        service.cancelOrder(bastion.getId(), orders.getFirst().getId());

        assertEquals(100, bastion.getTreasuryGp());
        assertEquals(OrderStatus.CANCELLED, orders.getFirst().getStatus());
        assertEquals(PlayerBastionLedgerEntry.Reason.REFUND, ledger.getLast().getReason());
    }

    @Test
    void onlyMasterManagesTreasuryAndItCannotGoNegative() {
        actAs(playerId);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ApiException.class,
                () -> service.adjustTreasury(bastion.getId(), new ActivityRequests.Treasury(500L, null))).getStatus());

        actAs(masterId);
        service.adjustTreasury(bastion.getId(), new ActivityRequests.Treasury(500L, "Награда за дракона"));
        assertEquals(600, bastion.getTreasuryGp());

        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class,
                () -> service.adjustTreasury(bastion.getId(), new ActivityRequests.Treasury(-700L, null))).getStatus());
    }

    /** Тесное базовое сооружение: 500 зм и 20 дней — готово на третьем ходу. */
    @Test
    void basicFacilityIsBuiltOverTurns() {
        bastion.setTreasuryGp(600);
        actAs(playerId);

        service.buildBasic(bastion.getId(), member.getId(),
                new ActivityRequests.BuildBasic("bedroom", FacilitySpace.CRAMPED));

        PlayerBastionFacility bedroom = member.getFacilities().getLast();
        assertEquals(100, bastion.getTreasuryGp());
        assertEquals(FacilityStatus.BUILDING, bedroom.getStatus());
        assertEquals(3, bedroom.getReadyOnTurn());

        actAs(masterId);
        service.performTurn(bastion.getId(), new ActivityRequests.Turn(null, null));
        service.performTurn(bastion.getId(), new ActivityRequests.Turn(null, null));
        assertEquals(FacilityStatus.BUILDING, bedroom.getStatus());
        service.performTurn(bastion.getId(), new ActivityRequests.Turn(null, null));
        assertEquals(FacilityStatus.READY, bedroom.getStatus());
        assertNull(bedroom.getReadyOnTurn());
    }

    @Test
    void facilityUnderConstructionTakesNoOrders() {
        library.setStatus(FacilityStatus.BUILDING);
        library.setReadyOnTurn(2);
        actAs(playerId);

        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH, null, null))).getStatus());
    }

    private void actAs(UUID userId) {
        when(userService.getCurrentUserId()).thenReturn(Optional.of(userId));
    }

    private GameMembership membership(UUID userId, GameRole role) {
        return new GameMembership(gameId, "Проклятие Страда", masterId, userId, role);
    }

    private static BastionFacility library() {
        BastionFacility facility = new BastionFacility();
        facility.setUrl("library");
        facility.setName("Библиотека");
        facility.setCategory(FacilityCategory.SPECIAL);
        facility.setLevel(5L);
        facility.setSpace(FacilitySpace.ROOMY);
        facility.setOrders(List.of(BastionOrder.RESEARCH));
        FacilityOrderOption option = new FacilityOrderOption();
        option.setOrder(BastionOrder.RESEARCH);
        option.setName("тему");
        option.setDays(7);
        option.setCost(10);
        facility.setOrderOptions(List.of(option));
        return facility;
    }

    private static BastionFacility basic(String url) {
        BastionFacility facility = new BastionFacility();
        facility.setUrl(url);
        facility.setName(url);
        facility.setCategory(FacilityCategory.BASIC);
        return facility;
    }

    @SuppressWarnings("unused")
    private static <T> T unused(T value) {
        return eq(value);
    }
}
