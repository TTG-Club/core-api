package club.ttg.dnd5.domain.bastion.player.activity;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilityChoice;
import club.ttg.dnd5.domain.bastion.model.FacilityChoiceSource;
import club.ttg.dnd5.domain.bastion.model.FacilityOrderOption;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.model.MenagerieBeast;
import club.ttg.dnd5.domain.bastion.player.activity.PlayerBastionOrder.OrderStatus;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembership;
import club.ttg.dnd5.domain.bastion.player.client.GameRole;
import club.ttg.dnd5.domain.bastion.player.event.BastionDice;
import club.ttg.dnd5.domain.bastion.player.event.BastionEventService;
import club.ttg.dnd5.domain.bastion.player.event.BastionEventType;
import club.ttg.dnd5.domain.bastion.player.event.PlayerBastionEvent;
import club.ttg.dnd5.domain.bastion.player.event.PlayerBastionEventRepository;
import club.ttg.dnd5.domain.bastion.player.model.FacilityStatus;
import club.ttg.dnd5.domain.bastion.player.model.MenagerieCreature;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionFacility;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionMember;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.model.SelectedChoice;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionEditorRepository;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionRepository;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionResponse;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionAccess;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionResponseMapper;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.domain.bastion.service.FacilityChoiceOptions;
import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.tool.Tool;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.repository.MagicItemRepository;
import club.ttg.dnd5.domain.user.service.UserService;
import club.ttg.dnd5.exception.ApiException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
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
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final PlayerBastionEventRepository eventRepository = mock(PlayerBastionEventRepository.class);
    private final MagicItemRepository magicItemRepository = mock(MagicItemRepository.class);
    private final QueueDice dice = new QueueDice();
    private final PlayerBastionActivityService service = new PlayerBastionActivityService(
            bastionRepository, facilityRepository, orderRepository, ledgerRepository, turnRepository,
            new PlayerBastionAccess(bastionRepository, mock(PlayerBastionEditorRepository.class), client, userService),
            new PlayerBastionResponseMapper(facilityRepository, mock(PlayerBastionEditorRepository.class),
                    new FacilityChoiceOptions(itemRepository)),
            mock(EntityManager.class),
            new FacilityChoiceOptions(itemRepository),
            new BastionEventService(eventRepository, ledgerRepository, dice),
            eventRepository, magicItemRepository);

    private final UUID gameId = UUID.randomUUID();
    private final UUID masterId = UUID.randomUUID();
    private final UUID playerId = UUID.randomUUID();
    private final UUID otherPlayerId = UUID.randomUUID();

    private final List<PlayerBastionOrder> orders = new ArrayList<>();
    private final List<PlayerBastionLedgerEntry> ledger = new ArrayList<>();
    private final List<PlayerBastionTurn> turns = new ArrayList<>();
    private final List<PlayerBastionEvent> events = new ArrayList<>();
    private final Map<String, BastionFacility> reference = Map.of(
            "library", library(),
            "barrack", barrack(),
            "workshop", workshop(),
            "menagerie", menagerie(),
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

        when(eventRepository.save(any(PlayerBastionEvent.class))).thenAnswer(invocation -> {
            PlayerBastionEvent event = invocation.getArgument(0);
            if (event.getId() == null) {
                event.setId(UUID.randomUUID());
                events.add(event);
            }
            return event;
        });
        when(eventRepository.existsByBastionIdAndTurn(any(), anyInt())).thenAnswer(invocation -> events.stream()
                .anyMatch(event -> event.getTurn() == invocation.<Integer>getArgument(1)));
        when(eventRepository.findById(any())).thenAnswer(invocation -> events.stream()
                .filter(event -> event.getId().equals(invocation.getArgument(0)))
                .findFirst());

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

    /** Итог приказа записывают после хода, а не в окне хода. */
    @Test
    void orderResultIsWrittenAfterTurn() {
        actAs(playerId);
        service.giveOrder(bastion.getId(), new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH,
                "тему", null));
        UUID orderId = orders.getFirst().getId();
        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class, () -> service.writeOrderResult(
                bastion.getId(), orderId, new ActivityRequests.OrderResult("рано"))).getStatus());

        actAs(masterId);
        service.performTurn(bastion.getId(), new ActivityRequests.Turn(null, null));

        actAs(otherPlayerId);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ApiException.class, () -> service.writeOrderResult(
                bastion.getId(), orderId, new ActivityRequests.OrderResult("чужой итог"))).getStatus());

        actAs(playerId);
        service.writeOrderResult(bastion.getId(), orderId, new ActivityRequests.OrderResult(" Три факта о Страде "));
        assertEquals("Три факта о Страде", orders.getFirst().getResult());
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
        assertTrue(events.isEmpty());

        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH, null, null))).getStatus());

        actAs(masterId);
        service.performTurn(bastion.getId(), new ActivityRequests.Turn("Гость из соседнего королевства", Map.of()));

        assertTrue(turns.getFirst().isMaintain());
        assertEquals("Гость из соседнего королевства", turns.getFirst().getEvent());
    }

    /** «Обслуживать» отменяет приказы, которые ещё выполняются, только после подтверждения. */
    @Test
    void maintainCancelsActiveOrdersWhenConfirmed() {
        actAs(playerId);
        service.giveOrder(bastion.getId(), new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH,
                "тему", null));

        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(null, BastionOrder.MAINTAIN, null, null))).getStatus());

        service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(null, BastionOrder.MAINTAIN, null, null, true, null));

        assertEquals(OrderStatus.CANCELLED, orders.getFirst().getStatus());
        assertEquals(BastionOrder.MAINTAIN, orders.getLast().getOrder());
        assertEquals(100, bastion.getTreasuryGp());
    }

    /** Приказ прошлого хода отменяется без возврата: работа уже идёт. */
    @Test
    void maintainCancelsEarlierOrderWithoutRefund() {
        actAs(playerId);
        service.giveOrder(bastion.getId(), new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH,
                "тему", null));
        bastion.setTurn(bastion.getTurn() + 1);

        service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(null, BastionOrder.MAINTAIN, null, null, true, null));

        assertEquals(OrderStatus.CANCELLED, orders.getFirst().getStatus());
        assertEquals(90, bastion.getTreasuryGp());
    }

    @Test
    void playerCannotCancelForeignOrdersWithMaintain() {
        actAs(playerId);
        service.giveOrder(bastion.getId(), new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH,
                null, null));

        PlayerBastionMember other = new PlayerBastionMember();
        other.setId(UUID.randomUUID());
        other.setBastion(bastion);
        other.setUserId(otherPlayerId);
        bastion.getMembers().add(other);

        actAs(otherPlayerId);
        ApiException error = assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(null, BastionOrder.MAINTAIN, null, null, true, null)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        assertEquals(OrderStatus.ACTIVE, orders.getFirst().getStatus());
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

    /** Три блока стены: 750 зм и 30 дней — достроены на пятом ходу, вторую партию не начать, пока идёт первая. */
    @Test
    void defenseWallIsBuiltOverTurns() {
        bastion.setTreasuryGp(1000);
        actAs(playerId);

        service.buildWall(bastion.getId(), new ActivityRequests.BuildWall(3));

        assertEquals(250, bastion.getTreasuryGp());
        assertEquals(3, bastion.getDefenseWallSquaresBuilding());
        assertEquals(5, bastion.getDefenseWallReadyOnTurn());
        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class,
                () -> service.buildWall(bastion.getId(), new ActivityRequests.BuildWall(1))).getStatus());

        actAs(masterId);
        for (int turn = 0; turn < 4; turn++) {
            service.performTurn(bastion.getId(), new ActivityRequests.Turn(null, null));
        }
        assertEquals(0, bastion.getDefenseWallSquares());
        service.performTurn(bastion.getId(), new ActivityRequests.Turn(null, null));
        assertEquals(3, bastion.getDefenseWallSquares());
        assertEquals(0, bastion.getDefenseWallSquaresBuilding());
        assertNull(bastion.getDefenseWallReadyOnTurn());
    }

    @Test
    void defenseWallNeedsTreasury() {
        bastion.setTreasuryGp(200);
        actAs(playerId);

        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class,
                () -> service.buildWall(bastion.getId(), new ActivityRequests.BuildWall(1))).getStatus());
        assertEquals(0, bastion.getDefenseWallSquaresBuilding());
    }

    @Test
    void facilityUnderConstructionTakesNoOrders() {
        library.setStatus(FacilityStatus.BUILDING);
        library.setReadyOnTurn(2);
        actAs(playerId);

        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH, null, null))).getStatus());
    }

    @Test
    void inSetupPlayerRemovesOwnFacilityButNotForeign() {
        bastion.setStatus(PlayerBastionStatus.SETUP);

        actAs(otherPlayerId);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ApiException.class,
                () -> service.removeFacility(bastion.getId(), library.getId())).getStatus());

        actAs(playerId);
        service.removeFacility(bastion.getId(), library.getId());

        assertTrue(member.getFacilities().isEmpty());
    }

    @Test
    void inActiveBastionOnlyMasterRemovesFacility() {
        actAs(playerId);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ApiException.class,
                () -> service.removeFacility(bastion.getId(), library.getId())).getStatus());

        actAs(masterId);
        service.removeFacility(bastion.getId(), library.getId());

        assertTrue(member.getFacilities().isEmpty());
    }

    @Test
    void barrackRecruitsFourDefendersForFreeUpToCapacity() {
        PlayerBastionFacility barrack = addBarrack();
        barrack.setDefenders(10);

        actAs(playerId);
        service.giveOrder(bastion.getId(), new ActivityRequests.GiveOrder(barrack.getId(), BastionOrder.RECRUIT,
                "Защитники бастиона", null));
        assertEquals(100, bastion.getTreasuryGp());
        assertEquals(0, orders.getFirst().getCostGp());

        actAs(masterId);
        service.performTurn(bastion.getId(), new ActivityRequests.Turn(null, Map.of()));
        assertEquals(12, barrack.getDefenders());
        assertTrue(turns.getFirst().getSummary().getFirst().contains("защитников +2 (12 из 12)"),
                turns.getFirst().getSummary().getFirst());

        actAs(playerId);
        ApiException full = assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(barrack.getId(), BastionOrder.RECRUIT, null, null)));
        assertEquals(HttpStatus.CONFLICT, full.getStatus());
    }

    @Test
    void vastBarrackHoldsTwentyFourDefenders() {
        PlayerBastionFacility barrack = addBarrack();
        barrack.setSpace(FacilitySpace.VAST);
        barrack.setDefenders(12);

        actAs(playerId);
        service.giveOrder(bastion.getId(), new ActivityRequests.GiveOrder(barrack.getId(), BastionOrder.RECRUIT,
                null, null));
        actAs(masterId);
        PlayerBastionResponse response = service.performTurn(bastion.getId(), new ActivityRequests.Turn(null, Map.of()));

        PlayerBastionResponse.Facility shown = response.members().getFirst().facilities().stream()
                .filter(facility -> facility.id().equals(barrack.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(16, shown.defenders());
        assertEquals(24, shown.defenderCapacity());
        assertNull(response.members().getFirst().facilities().stream()
                .filter(facility -> facility.id().equals(library.getId()))
                .findFirst()
                .orElseThrow()
                .defenderCapacity());
    }

    @Test
    void workshopCraftsOnlyItemsOfChosenTools() {
        when(itemRepository.findArtisanTools()).thenReturn(List.of(
                tool("smiths-tools", "Инструменты кузнеца", new EntityRef("dagger", "Кинжал"),
                        new EntityRef("chain", "Цепь")),
                tool("weavers-tools", "Инструменты ткача", new EntityRef("rope", "Верёвка"))));
        when(itemRepository.findAllById(any())).thenReturn(List.of(
                priced("dagger", "2", Coin.GC), priced("chain", "150", Coin.GC)));
        PlayerBastionFacility workshop = new PlayerBastionFacility();
        workshop.setId(UUID.randomUUID());
        workshop.setMember(member);
        workshop.setFacilityUrl("workshop");
        workshop.setSpace(FacilitySpace.ROOMY);
        workshop.setPrerequisiteConfirmed(true);
        workshop.setChoices(new ArrayList<>(List.of(
                new SelectedChoice("Инструменты ремесленников", List.of("Инструменты кузнеца")))));
        member.getFacilities().add(workshop);

        actAs(playerId);
        ApiException foreign = assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(workshop.getId(), BastionOrder.CRAFT, null, null, null, "rope")));
        assertEquals(HttpStatus.BAD_REQUEST, foreign.getStatus());

        PlayerBastionResponse response = service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(workshop.getId(), BastionOrder.CRAFT, null, null, null, "chain"));
        assertEquals("chain", orders.getFirst().getItemUrl());
        assertEquals("Цепь", orders.getFirst().getItemName());
        // 150 зм / (10 зм × 2 наёмника) = 8 дней → 2 хода
        assertEquals(bastion.getTurn() + 2, orders.getFirst().getCompletesOnTurn());
        assertEquals(List.of(
                new PlayerBastionResponse.CraftableItem("dagger", "Кинжал", 2.0, 1),
                new PlayerBastionResponse.CraftableItem("chain", "Цепь", 150.0, 8)),
                response.members().getFirst().facilities().stream()
                .filter(facility -> facility.id().equals(workshop.getId()))
                .findFirst()
                .orElseThrow()
                .craftableItems());
        assertNull(response.members().getFirst().facilities().stream()
                .filter(facility -> facility.id().equals(library.getId()))
                .findFirst()
                .orElseThrow()
                .craftableItems());
    }

    @Test
    void itemIsChosenOnlyForCraft() {
        actAs(playerId);
        ApiException wrong = assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH, null, null, null, "dagger")));
        assertEquals(HttpStatus.BAD_REQUEST, wrong.getStatus());
    }

    private static Item tool(String url, String name, EntityRef... crafts) {
        Tool tool = new Tool();
        tool.setCrafts(List.of(crafts));
        Item item = new Item();
        item.setUrl(url);
        item.setName(name);
        item.setTool(tool);
        return item;
    }

    @Test
    void menagerieRecruitsBeastFromTableForItsCostInSevenDays() {
        PlayerBastionFacility menagerie = addMenagerie();
        bastion.setTreasuryGp(2000);

        actAs(playerId);
        service.giveOrder(bastion.getId(), new ActivityRequests.GiveOrder(menagerie.getId(), BastionOrder.RECRUIT,
                null, null, null, null, new ActivityRequests.RecruitCreature(MenagerieBeast.LION, null, null, null)));
        assertEquals(1000, bastion.getTreasuryGp());
        assertEquals("Лев", orders.getFirst().getOptionName());
        assertEquals(bastion.getTurn() + 1, orders.getFirst().getCompletesOnTurn());

        actAs(masterId);
        PlayerBastionResponse response = service.performTurn(bastion.getId(), new ActivityRequests.Turn(null, Map.of()));
        assertEquals(1, menagerie.getCreatures().size());
        assertTrue(menagerie.getCreatures().getFirst().isDefender());
        assertTrue(turns.getFirst().getSummary().getFirst().contains("Лев поселяется в вольерах"),
                turns.getFirst().getSummary().getFirst());
        PlayerBastionResponse.Menagerie shown = response.members().getFirst().facilities().stream()
                .filter(facility -> facility.id().equals(menagerie.getId()))
                .findFirst()
                .orElseThrow()
                .menagerie();
        assertEquals(16, shown.capacity());
        assertEquals(4, shown.occupied());
        assertEquals(MenagerieBeast.values().length, shown.beasts().size());
    }

    @Test
    void menagerieHoldsFourLargeOrSixteenSmallCreatures() {
        PlayerBastionFacility menagerie = addMenagerie();
        for (int i = 0; i < 3; i++) {
            menagerie.getCreatures().add(creature("Тигр", Size.LARGE));
        }
        menagerie.getCreatures().add(creature("Шакал", Size.SMALL));
        bastion.setTreasuryGp(5000);

        actAs(playerId);
        ApiException full = assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(menagerie.getId(), BastionOrder.RECRUIT, null, null, null, null,
                        new ActivityRequests.RecruitCreature(MenagerieBeast.LION, null, null, null))));
        assertEquals(HttpStatus.CONFLICT, full.getStatus());

        service.giveOrder(bastion.getId(), new ActivityRequests.GiveOrder(menagerie.getId(), BastionOrder.RECRUIT,
                null, null, null, null, new ActivityRequests.RecruitCreature(MenagerieBeast.HYENA, null, null, null)));
        assertEquals(50, orders.getFirst().getCostGp());
    }

    @Test
    void creatureOutsideTableNeedsMasterAndCostsByChallenge() {
        PlayerBastionFacility menagerie = addMenagerie();
        bastion.setTreasuryGp(5000);
        ActivityRequests.RecruitCreature griffon =
                new ActivityRequests.RecruitCreature(null, "Грифон", Size.LARGE, "2");

        actAs(playerId);
        ApiException player = assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(menagerie.getId(), BastionOrder.RECRUIT, null, null, null, null, griffon)));
        assertEquals(HttpStatus.FORBIDDEN, player.getStatus());

        actAs(masterId);
        ApiException tooStrong = assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(menagerie.getId(), BastionOrder.RECRUIT, null, null, null, null,
                        new ActivityRequests.RecruitCreature(null, "Мантикора", Size.LARGE, "5"))));
        assertEquals(HttpStatus.BAD_REQUEST, tooStrong.getStatus());

        service.giveOrder(bastion.getId(), new ActivityRequests.GiveOrder(menagerie.getId(), BastionOrder.RECRUIT,
                null, null, null, null, griffon));
        assertEquals(2000, orders.getFirst().getCostGp());
        assertEquals("2", orders.getFirst().getCreature().getChallenge());
    }

    @Test
    void creatureCanLeaveDefendersOrBeRemoved() {
        PlayerBastionFacility menagerie = addMenagerie();
        MenagerieCreature tiger = creature("Тигр", Size.LARGE);
        menagerie.getCreatures().add(tiger);

        actAs(playerId);
        service.setCreatureDefender(bastion.getId(), menagerie.getId(), tiger.getId(),
                new ActivityRequests.CreatureDefender(false));
        assertEquals(false, tiger.isDefender());

        service.removeCreature(bastion.getId(), menagerie.getId(), tiger.getId());
        assertTrue(menagerie.getCreatures().isEmpty());
    }

    /** Сразу после «Обслуживать» — бросок за персонажа; атака уносит защитников за каждую 1 на 6к6. */
    @Test
    void maintainRollsEventAndAttackKillsDefenders() {
        PlayerBastionFacility barrack = addBarrack();
        barrack.setDefenders(3);
        PlayerBastionFacility menagerie = addMenagerie();
        menagerie.getCreatures().add(creature("Лев", Size.LARGE));
        dice.push(52, 1, 1, 3, 4, 5, 6, 4, 1);

        maintainTurn();

        PlayerBastionEvent event = events.getFirst();
        assertEquals(BastionEventType.ATTACK, event.getType());
        assertEquals(member.getId(), event.getMemberId());
        assertEquals(List.of("Лев", "защитник из «Барак»"), event.getDetails().getLost());
        assertEquals(2, barrack.getDefenders());
        assertTrue(menagerie.getCreatures().isEmpty());
        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class,
                () -> service.cancelOrder(bastion.getId(), orders.getFirst().getId())).getStatus());
    }

    /** Бастион, окружённый стенами, бросает потери на 4к6 вместо 6к6. */
    @Test
    void enclosedBastionRollsTwoDiceFewerOnAttack() {
        PlayerBastionFacility barrack = addBarrack();
        barrack.setDefenders(5);
        bastion.setDefenseWallsEnclosed(true);
        dice.push(52, 1, 3, 4, 1);

        maintainTurn();

        PlayerBastionEvent event = events.getFirst();
        assertEquals(List.of(1, 3, 4, 1), event.getDetails().getDice());
        assertEquals(3, barrack.getDefenders());
        assertTrue(event.getSummary().contains("4к6"));
    }

    /** Без защитников атака повреждает случайное специализированное сооружение на следующий ход. */
    @Test
    void attackWithoutDefendersDisablesFacilityForNextTurn() {
        dice.push(55, 1);
        maintainTurn();

        assertEquals(1, library.getUnavailableOnTurn());
        assertEquals(1, bastion.getTurn());
        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class, () -> service.giveOrder(bastion.getId(),
                new ActivityRequests.GiveOrder(library.getId(), BastionOrder.RESEARCH, null, null))).getStatus());
    }

    /** Взятку за наёмника платят из казны; отказ оставляет сооружение без наёмника на ход. */
    @Test
    void criminalHirelingIsBribedOrArrested() {
        reference.get("library").setHirelings(1);
        dice.push(57, 1, 3);
        maintainTurn();

        PlayerBastionEvent event = events.getFirst();
        assertTrue(event.isPending());
        assertEquals(300, event.getDetails().getAmountGp());
        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class, () -> service.decideEvent(bastion.getId(),
                event.getId(), new ActivityRequests.EventDecision(PlayerBastionEvent.Decision.PAY, null, null)))
                .getStatus());
        actAs(otherPlayerId);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ApiException.class, () -> service.decideEvent(bastion.getId(),
                event.getId(), new ActivityRequests.EventDecision(PlayerBastionEvent.Decision.REFUSE, null, null)))
                .getStatus());

        actAs(playerId);
        service.decideEvent(bastion.getId(), event.getId(),
                new ActivityRequests.EventDecision(PlayerBastionEvent.Decision.REFUSE, null, null));
        assertEquals(PlayerBastionEvent.Status.RESOLVED, event.getStatus());
        assertEquals(1, library.getUnavailableOnTurn());
        assertEquals(100, bastion.getTreasuryGp());
        assertEquals(1, library.missingHirelings(1));
        assertEquals(0, library.missingHirelings(2));
    }

    /** Пропавшие наёмники не считаются в сооружении, пока им не найдут замену в конце следующего хода. */
    @Test
    void lostHirelingsAreMissingUntilReplaced() {
        reference.get("library").setHirelings(2);
        dice.push(78, 1);
        maintainTurn();

        assertEquals(BastionEventType.LOST_HIRELINGS, events.getFirst().getType());
        assertEquals(1, library.getUnavailableOnTurn());
        assertEquals(2, library.missingHirelings(0));
        assertEquals(2, library.missingHirelings(1));
        assertEquals(0, library.missingHirelings(2));
    }

    /** Удачный случай за 500 зм бросает событие ещё раз, перебрасывая повтор удачного случая. */
    @Test
    void opportunityPaysAndRollsAgainWithoutRepeating() {
        bastion.setTreasuryGp(1000);
        dice.push(60);
        maintainTurn();

        dice.push(61, 10, 8);
        service.decideEvent(bastion.getId(), events.getFirst().getId(),
                new ActivityRequests.EventDecision(PlayerBastionEvent.Decision.PAY, null, null));

        assertEquals(500, bastion.getTreasuryGp());
        assertEquals(2, events.size());
        assertEquals(BastionEventType.ALL_IS_WELL, events.getLast().getType());
        assertEquals(events.getFirst().getId(), events.getLast().getParentId());
        assertEquals("Кто-то подумал, что увидел привидение.", events.getLast().getDetails().getDetail());
    }

    /** Меньше 10 на к6 отправленных защитников — половина награды и гибель одного защитника. */
    @Test
    void requestForAidHalvesRewardAndCostsDefenderOnLowRoll() {
        PlayerBastionFacility barrack = addBarrack();
        barrack.setDefenders(2);
        dice.push(95);
        maintainTurn();

        PlayerBastionEvent event = events.getFirst();
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ApiException.class, () -> service.decideEvent(
                bastion.getId(), event.getId(),
                new ActivityRequests.EventDecision(PlayerBastionEvent.Decision.SEND, 3, null))).getStatus());

        dice.push(2, 3, 4, 1);
        service.decideEvent(bastion.getId(), event.getId(),
                new ActivityRequests.EventDecision(PlayerBastionEvent.Decision.SEND, 2, null));

        assertEquals(300, bastion.getTreasuryGp());
        assertEquals(1, barrack.getDefenders());
        assertEquals(200, event.getDetails().getAmountGp());
    }

    /** Беженцы платят и остаются; монстр-гость отбивает атаку без потерь, беженцы при атаке уходят. */
    @Test
    void refugeesLeaveAndMonsterGuestProtectsOnAttack() {
        PlayerBastionFacility barrack = addBarrack();
        barrack.setDefenders(1);
        dice.push(85, 2, 3, 2);
        maintainTurn();
        assertEquals(300, bastion.getTreasuryGp());
        assertEquals(5, bastion.getGuests().getFirst().getCount());

        dice.push(76, 4);
        maintainTurn();
        assertEquals(2, bastion.getGuests().size());

        dice.push(53);
        maintainTurn();

        assertTrue(bastion.getGuests().isEmpty());
        assertEquals(1, barrack.getDefenders());
        assertTrue(events.getLast().getDetails().getDice().isEmpty());
    }

    /** Ход без приказов — тоже обслуживание; известный гость уезжает к следующему ходу. */
    @Test
    void turnWithoutOrdersRollsEventAndCelebrityLeaves() {
        dice.push(74, 1);
        actAs(masterId);
        service.performTurn(bastion.getId(), new ActivityRequests.Turn(null, Map.of()));

        assertEquals(BastionEventType.GUEST, events.getFirst().getType());
        assertEquals(0, events.getFirst().getTurn());
        assertTrue(turns.getFirst().getSummary().contains(
                "Известная личность уезжает и оставляет рекомендательное письмо"));
        assertTrue(bastion.getGuests().isEmpty());
    }

    /** Предмет волшебного открытия записывает игрок. */
    @Test
    void magicalDiscoveryRecordsChosenItem() {
        dice.push(81);
        maintainTurn();

        PlayerBastionEvent event = events.getFirst();
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ApiException.class, () -> service.decideEvent(
                bastion.getId(), event.getId(),
                new ActivityRequests.EventDecision(PlayerBastionEvent.Decision.RECORD, null, " "))).getStatus());
        service.decideEvent(bastion.getId(), event.getId(),
                new ActivityRequests.EventDecision(PlayerBastionEvent.Decision.RECORD, null, "Зелье полёта"));
        assertEquals("Зелье полёта", event.getNote());
        assertEquals(PlayerBastionEvent.Status.RESOLVED, event.getStatus());
    }

    /** Предмет выбирают из раздела магических предметов: название берётся из справочника, ссылка сохраняется. */
    @Test
    void magicalDiscoveryRecordsCatalogItem() {
        MagicItem potion = new MagicItem();
        potion.setUrl("potion-of-flying-dmg");
        potion.setName("Зелье полёта");
        when(magicItemRepository.findById("potion-of-flying-dmg")).thenReturn(Optional.of(potion));
        dice.push(81);
        maintainTurn();

        PlayerBastionEvent event = events.getFirst();
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ApiException.class, () -> service.decideEvent(
                bastion.getId(), event.getId(), new ActivityRequests.EventDecision(
                        PlayerBastionEvent.Decision.RECORD, null, null, "missing"))).getStatus());
        service.decideEvent(bastion.getId(), event.getId(), new ActivityRequests.EventDecision(
                PlayerBastionEvent.Decision.RECORD, null, null, "potion-of-flying-dmg"));
        assertEquals("Зелье полёта", event.getNote());
        assertEquals("potion-of-flying-dmg", event.getNoteUrl());
    }

    /** Кости с заранее заданными бросками; когда они кончаются, выпадает 1. */
    private static final class QueueDice extends BastionDice {
        private final Deque<Integer> queue = new ArrayDeque<>();

        void push(int... values) {
            Arrays.stream(values).forEach(queue::addLast);
        }

        @Override
        public int roll(int sides) {
            Integer value = queue.pollFirst();
            return value == null ? 1 : value;
        }
    }

    private PlayerBastionFacility addMenagerie() {
        PlayerBastionFacility menagerie = new PlayerBastionFacility();
        menagerie.setId(UUID.randomUUID());
        menagerie.setMember(member);
        menagerie.setFacilityUrl("menagerie");
        menagerie.setSpace(FacilitySpace.VAST);
        menagerie.setPrerequisiteConfirmed(true);
        member.getFacilities().add(menagerie);
        return menagerie;
    }

    private static MenagerieCreature creature(String name, Size size) {
        MenagerieCreature creature = new MenagerieCreature();
        creature.setId(UUID.randomUUID().toString());
        creature.setName(name);
        creature.setSize(size);
        return creature;
    }

    private static BastionFacility menagerie() {
        BastionFacility facility = new BastionFacility();
        facility.setUrl("menagerie");
        facility.setName("Зверинец");
        facility.setCategory(FacilityCategory.SPECIAL);
        facility.setLevel(13L);
        facility.setSpace(FacilitySpace.VAST);
        facility.setMenagerie(true);
        facility.setOrders(List.of(BastionOrder.RECRUIT));
        return facility;
    }

    private static Item priced(String url, String cost, Coin coin) {
        Item item = new Item();
        item.setUrl(url);
        item.setCost(cost);
        item.setCoin(coin);
        return item;
    }

    private static BastionFacility workshop() {
        BastionFacility facility = new BastionFacility();
        facility.setUrl("workshop");
        facility.setName("Мастерская");
        facility.setCategory(FacilityCategory.SPECIAL);
        facility.setLevel(5L);
        facility.setSpace(FacilitySpace.ROOMY);
        facility.setHirelings(2);
        facility.setOrders(List.of(BastionOrder.CRAFT));
        FacilityChoice tools = new FacilityChoice();
        tools.setName("Инструменты ремесленников");
        tools.setCount(6);
        tools.setSource(FacilityChoiceSource.ARTISAN_TOOLS);
        facility.setChoices(List.of(tools));
        return facility;
    }

    private PlayerBastionFacility addBarrack() {
        PlayerBastionFacility barrack = new PlayerBastionFacility();
        barrack.setId(UUID.randomUUID());
        barrack.setMember(member);
        barrack.setFacilityUrl("barrack");
        barrack.setSpace(FacilitySpace.ROOMY);
        barrack.setPrerequisiteConfirmed(true);
        member.getFacilities().add(barrack);
        return barrack;
    }

    /** Игрок отдаёт «Обслуживать», мастер делает ход: тогда и бросаются события бастиона. */
    private void maintainTurn() {
        actAs(playerId);
        service.giveOrder(bastion.getId(), new ActivityRequests.GiveOrder(null, BastionOrder.MAINTAIN, null, null));
        actAs(masterId);
        service.performTurn(bastion.getId(), new ActivityRequests.Turn(null, Map.of()));
        actAs(playerId);
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

    private static BastionFacility barrack() {
        BastionFacility facility = new BastionFacility();
        facility.setUrl("barrack");
        facility.setName("Барак");
        facility.setCategory(FacilityCategory.SPECIAL);
        facility.setLevel(5L);
        facility.setSpace(FacilitySpace.ROOMY);
        facility.setBarrack(true);
        facility.setOrders(List.of(BastionOrder.RECRUIT));
        FacilityOrderOption option = new FacilityOrderOption();
        option.setOrder(BastionOrder.RECRUIT);
        option.setName("Защитники бастиона");
        option.setDays(7);
        option.setCost(50);
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
