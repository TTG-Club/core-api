package club.ttg.dnd5.domain.bastion.player.activity;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.model.BastionRules;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilityEnlargement;
import club.ttg.dnd5.domain.bastion.model.FacilityOrderOption;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.activity.PlayerBastionLedgerEntry.Reason;
import club.ttg.dnd5.domain.bastion.player.activity.PlayerBastionOrder.OrderStatus;
import club.ttg.dnd5.domain.bastion.player.event.BastionEventService;
import club.ttg.dnd5.domain.bastion.player.event.PlayerBastionEvent;
import club.ttg.dnd5.domain.bastion.player.event.PlayerBastionEventRepository;
import club.ttg.dnd5.domain.bastion.player.model.FacilityStatus;
import club.ttg.dnd5.domain.bastion.player.model.MenagerieCreature;
import club.ttg.dnd5.domain.bastion.player.model.StableAnimal;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionFacility;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionMember;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionRepository;
import club.ttg.dnd5.domain.bastion.player.rest.dto.FacilitySetupRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionResponse;
import club.ttg.dnd5.domain.bastion.player.service.BastionViewer;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionAccess;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionResponseMapper;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionSetupRules;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.domain.bastion.service.FacilityChoiceOptions;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionLabel;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.repository.MagicItemRepository;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Жизнь запущенного бастиона: приказы, ходы, казна, стройка, расширение и события.
 *
 * <p>Ход бастиона — 7 игровых дней. Сроки из справочника переводятся в ходы с
 * округлением вверх ({@link #turnsFor}): 7 дней — 1 ход, 20 дней — 3 хода.</p>
 *
 * <p>Права:
 * <ul>
 *     <li>ход и казна — только мастер;</li>
 *     <li>приказ сооружению, стройка и расширение — мастер или игрок, чей это персонаж;</li>
 *     <li>«Обслуживать» — мастер или любой игрок с доступом к бастиону;</li>
 *     <li>убрать сооружение — только мастер (так делается замена при повышении уровня).</li>
 * </ul></p>
 */
@Service
@RequiredArgsConstructor
public class PlayerBastionActivityService {
    /** Дней в ходе бастиона. */
    public static final int DAYS_PER_TURN = 7;
    private static final int JOURNAL_LIMIT = 100;

    private final PlayerBastionRepository bastionRepository;
    private final BastionFacilityRepository facilityRepository;
    private final PlayerBastionOrderRepository orderRepository;
    private final PlayerBastionLedgerRepository ledgerRepository;
    private final PlayerBastionTurnRepository turnRepository;
    private final PlayerBastionAccess access;
    private final PlayerBastionResponseMapper mapper;
    private final EntityManager entityManager;
    private final FacilityChoiceOptions choiceOptions;
    private final BastionEventService events;
    private final PlayerBastionEventRepository eventRepository;
    private final MagicItemRepository magicItemRepository;

    /** Сколько ходов занимает дело на столько-то дней; меньше хода не бывает. */
    public static int turnsFor(Integer days) {
        if (days == null || days <= 0) {
            return 1;
        }
        return (days + DAYS_PER_TURN - 1) / DAYS_PER_TURN;
    }

    // ------------------------------------------------------------------ журналы

    @Transactional(readOnly = true)
    public ActivityResponse findActivity(UUID bastionId) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        boolean master = viewer.manager();

        Map<UUID, PlayerBastionFacility> facilities = facilitiesById(bastion);
        Map<String, BastionFacility> reference = loadReference(facilities.values().stream()
                .map(PlayerBastionFacility::getFacilityUrl));
        PageRequest page = PageRequest.of(0, JOURNAL_LIMIT);
        Map<UUID, String> characterByUser = charactersByUser(bastion);

        List<ActivityResponse.Order> orders = orderRepository.findAllByBastionIdOrderByCreatedAtDesc(bastionId, page)
                .stream()
                .map(order -> new ActivityResponse.Order(
                        order.getId(),
                        order.getFacilityId(),
                        Optional.ofNullable(order.getFacilityId())
                                .map(facilities::get)
                                .map(facility -> facilityName(facility, reference))
                                .orElse(null),
                        new BastionLabel(order.getOrder().name(), order.getOrder().getName()),
                        order.getOptionName(),
                        order.getItemUrl() == null ? null : new EntityRef(order.getItemUrl(), order.getItemName()),
                        order.getCostGp(),
                        order.getGivenOnTurn(),
                        order.getCompletesOnTurn(),
                        order.getStatus(),
                        order.getGivenBy(),
                        Optional.ofNullable(order.getFacilityId())
                                .map(facilities::get)
                                .map(facility -> facility.getMember().getCharacterName())
                                .orElseGet(() -> characterByUser.get(order.getGivenBy())),
                        order.getCreatedAt(),
                        order.getNote(),
                        order.getResult(),
                        order.getStatus() == OrderStatus.ACTIVE
                                && order.getGivenOnTurn() == bastion.getTurn()
                                && bastion.getStatus() == PlayerBastionStatus.ACTIVE
                                && (master || order.getGivenBy().equals(userId)),
                        order.getStatus() == OrderStatus.COMPLETED
                                && !order.getOrder().isBastionWide()
                                && bastion.getStatus() != PlayerBastionStatus.ARCHIVED
                                && (master || order.getGivenBy().equals(userId))))
                .toList();

        List<ActivityResponse.Turn> turns = turnRepository.findAllByBastionIdOrderByNumberDesc(bastionId, page).stream()
                .map(turn -> new ActivityResponse.Turn(turn.getNumber(), turn.isMaintain(), turn.getEvent(),
                        turn.getSummary(), turn.getPerformedBy(), turn.getPerformedAt()))
                .toList();

        List<ActivityResponse.LedgerEntry> ledger = ledgerRepository.findAllByBastionIdOrderByCreatedAtDesc(bastionId, page)
                .stream()
                .map(entry -> new ActivityResponse.LedgerEntry(entry.getTurn(), entry.getAmountGp(), entry.getReason(),
                        entry.getNote(), entry.getCreatedBy(), characterByUser.get(entry.getCreatedBy()),
                        entry.getCreatedAt()))
                .toList();

        Map<UUID, PlayerBastionMember> members = bastion.getMembers().stream()
                .collect(Collectors.toMap(PlayerBastionMember::getId, Function.identity()));
        boolean active = bastion.getStatus() == PlayerBastionStatus.ACTIVE;
        List<ActivityResponse.Event> bastionEvents = eventRepository
                .findAllByBastionIdOrderByCreatedAtDesc(bastionId, page)
                .stream()
                .map(event -> {
                    PlayerBastionMember member = members.get(event.getMemberId());
                    boolean canDecide = active && event.isPending()
                            && (master || member != null && member.getUserId().equals(userId));
                    return new ActivityResponse.Event(
                            event.getId(),
                            event.getMemberId(),
                            event.getCharacterName(),
                            event.getTurn(),
                            event.getRoll(),
                            new BastionLabel(event.getType().name(), event.getType().getName()),
                            event.getStatus(),
                            event.getSummary(),
                            event.getDetails(),
                            event.getDecision(),
                            event.getNote(),
                            event.getParentId(),
                            event.isPending() ? BastionEventService.decisions(event.getType()) : List.of(),
                            canDecide,
                            canDecide ? events.defenderCount(bastion, member, reference) : 0,
                            event.getNoteUrl());
                })
                .toList();

        return new ActivityResponse(orders, turns, ledger, bastionEvents);
    }

    // ------------------------------------------------------------------ приказы

    @Transactional
    public PlayerBastionResponse giveOrder(UUID bastionId, ActivityRequests.GiveOrder request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        boolean master = viewer.manager();
        requireActive(bastion);

        List<PlayerBastionOrder> givenThisTurn = orderRepository.findAllByBastionIdAndGivenOnTurnAndStatusNot(
                bastionId, bastion.getTurn(), OrderStatus.CANCELLED);

        PlayerBastionOrder order = new PlayerBastionOrder();
        order.setBastionId(bastionId);
        order.setOrder(request.order());
        order.setGivenOnTurn(bastion.getTurn());
        order.setGivenBy(userId);
        order.setNote(trimToNull(request.note()));

        if (request.order().isBastionWide()) {
            if (!master && !isMember(bastion, userId)) {
                throw forbidden("«Обслуживать» отдают мастер и игроки с доступом к бастиону");
            }
            if (givenThisTurn.stream().anyMatch(item -> item.getOrder().isBastionWide())) {
                throw conflict("В этот ход бастион уже обслуживают");
            }
            cancelForMaintain(bastion, userId, master, Boolean.TRUE.equals(request.cancelActive()));
            // События бастиона бросаются, когда мастер делает ход, а не сразу после приказа
            order.setCompletesOnTurn(bastion.getTurn() + 1);
        } else {
            PlayerBastionFacility facility = findFacility(bastion, request.facilityId());
            requireOwnerOrMaster(facility, userId, master, "Приказы сооружению отдаёт игрок этого персонажа или мастер");
            if (givenThisTurn.stream().anyMatch(item -> item.getOrder().isBastionWide())) {
                throw conflict("В этот ход бастион обслуживают: другие приказы отдать нельзя");
            }
            BastionFacility reference = referenceOf(facility);
            validateFacilityOrder(facility, reference, request.order());
            if (PlayerBastionResponseMapper.isUnavailable(facility)
                    && facility.getUnavailableOnTurn() == bastion.getTurn()) {
                throw conflict("«%s» не работает в этот ход: %s".formatted(reference.getName(),
                        Optional.ofNullable(facility.getUnavailableReason()).orElse("событие бастиона")
                                .toLowerCase()));
            }
            boolean busy = orderRepository.findAllByBastionIdAndStatus(bastionId, OrderStatus.ACTIVE).stream()
                    .anyMatch(item -> facility.getId().equals(item.getFacilityId()));
            if (busy) {
                throw conflict("Сооружение «%s» уже выполняет приказ".formatted(reference.getName()));
            }

            Optional<FacilityOrderOption> option = findOption(reference, request.order(), request.optionName());
            if (StringUtils.hasText(request.optionName()) && option.isEmpty()) {
                throw badRequest("У «%s» нет варианта «%s»".formatted(reference.getName(), request.optionName()));
            }
            option.map(FacilityOrderOption::getMinLevel)
                    .filter(minLevel -> minLevel > facility.getMember().getCharacterLevel())
                    .ifPresent(minLevel -> {
                        throw badRequest("Вариант «%s» доступен с %d уровня".formatted(request.optionName(), minLevel));
                    });

            boolean recruitDefenders = isRecruitDefenders(reference, request.order());
            if (recruitDefenders && facility.getDefenders() >= BastionRules.barrackCapacity(facility.getSpace())) {
                throw conflict("В «%s» больше не помещается защитников".formatted(reference.getName()));
            }
            MenagerieCreature creature = recruitCreature(facility, reference, request, master);
            StableAnimal animal = tradeAnimal(facility, reference, request, master);
            boolean sale = animal != null && trimToNull(request.trade().sellAnimalId()) != null;
            // Защитников барак вербует бесплатно, какую бы цену ни указал вариант; существо
            // зверинца стоит столько, сколько указано в таблице зверинца; покупка в конюшне —
            // обычную цену животного, продажа ничего не стоит
            long cost = recruitDefenders ? 0
                    : creature != null ? creature.getCost()
                    : animal != null ? (sale ? 0 : animal.getCost())
                    : option.map(FacilityOrderOption::getCost).orElse(0);
            order.setFacilityId(facility.getId());
            order.setOptionName(creature != null
                    ? creature.getName()
                    : animal != null ? "%s «%s»".formatted(sale ? "продажа" : "покупка", animal.getName())
                    : option.map(FacilityOrderOption::getName).orElse(null));
            order.setCreature(creature);
            order.setAnimal(animal);
            order.setAnimalSale(sale);
            Optional<PlayerBastionResponse.CraftableItem> item = craftItem(facility, reference, request);
            item.ifPresent(craft -> {
                order.setItemUrl(craft.url());
                order.setItemName(craft.name());
            });
            // Срок изготовления предмета считается по его цене и наёмникам, а не по варианту
            Integer days = creature != null
                    ? Integer.valueOf(BastionRules.MENAGERIE_RECRUIT_DAYS)
                    : animal != null ? Integer.valueOf(BastionRules.STABLE_TRADE_DAYS)
                    : item.map(PlayerBastionResponse.CraftableItem::days)
                            .or(() -> option.map(FacilityOrderOption::getDays))
                            .orElse(null);
            order.setCostGp(cost);
            order.setCompletesOnTurn(bastion.getTurn() + turnsFor(days));
            charge(bastion, cost, Reason.ORDER, "Приказ «%s» — %s".formatted(request.order().getName(),
                    reference.getName()), userId);
        }

        orderRepository.save(order);
        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    /**
     * Выводит существо зверинца из защитников бастиона или возвращает его туда.
     */
    @Transactional
    public PlayerBastionResponse setCreatureDefender(UUID bastionId, UUID facilityId, String creatureId,
                                                     ActivityRequests.CreatureDefender request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        requireActive(bastion);
        PlayerBastionFacility facility = findFacility(bastion, facilityId);
        requireOwnerOrMaster(facility, userId, viewer.manager(),
                "Существами зверинца распоряжается игрок этого персонажа или мастер");
        findCreature(facility, creatureId).setDefender(request.defender());
        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    /** Убирает существо из зверинца — например, оно погибло, защищая бастион. */
    @Transactional
    public PlayerBastionResponse removeCreature(UUID bastionId, UUID facilityId, String creatureId) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        requireActive(bastion);
        PlayerBastionFacility facility = findFacility(bastion, facilityId);
        requireOwnerOrMaster(facility, userId, viewer.manager(),
                "Существами зверинца распоряжается игрок этого персонажа или мастер");
        facility.getCreatures().remove(findCreature(facility, creatureId));
        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    /** Убирает животное из конюшни — например, персонаж забрал его в путешествие или оно погибло. */
    @Transactional
    public PlayerBastionResponse removeAnimal(UUID bastionId, UUID facilityId, String animalId) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        requireActive(bastion);
        PlayerBastionFacility facility = findFacility(bastion, facilityId);
        requireOwnerOrMaster(facility, userId, viewer.manager(),
                "Животными конюшни распоряжается игрок этого персонажа или мастер");
        facility.getAnimals().remove(findAnimal(facility, animalId));
        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    private static MenagerieCreature findCreature(PlayerBastionFacility facility, String creatureId) {
        return facility.getCreatures().stream()
                .filter(creature -> creatureId.equals(creature.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Существо %s не найдено".formatted(creatureId)));
    }

    /** Решение по событию бастиона: игрок персонажа, за которого бросали, или мастер. */
    @Transactional
    public PlayerBastionResponse decideEvent(UUID bastionId, UUID eventId, ActivityRequests.EventDecision request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        requireActive(bastion);
        PlayerBastionEvent event = eventRepository.findById(eventId)
                .filter(item -> item.getBastionId().equals(bastionId))
                .orElseThrow(() -> new EntityNotFoundException("Событие %s не найдено".formatted(eventId)));
        boolean owner = bastion.getMembers().stream()
                .anyMatch(member -> member.getId().equals(event.getMemberId()) && member.getUserId().equals(userId));
        if (!viewer.manager() && !owner) {
            throw forbidden("Решение по событию принимает игрок этого персонажа или мастер");
        }
        String itemUrl = trimToNull(request.itemUrl());
        String note = itemUrl == null ? request.note() : magicItemRepository.findById(itemUrl)
                .map(MagicItem::getName)
                .orElseThrow(() -> badRequest("Магический предмет %s не найден".formatted(itemUrl)));
        events.decide(bastion, event, request.decision(), request.defenders(), note,
                allReference(bastion), userId);
        if (event.getNote() != null) {
            event.setNoteUrl(itemUrl);
        }
        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    /** Отсылает гостя бастиона или находит беженцам новый дом. */
    @Transactional
    public PlayerBastionResponse dismissGuest(UUID bastionId, String guestId) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        requireActive(bastion);
        if (!viewer.manager() && !isMember(bastion, userId)) {
            throw forbidden("Гостей отсылают мастер и игроки бастиона");
        }
        if (!bastion.getGuests().removeIf(guest -> guestId.equals(guest.getId()))) {
            throw new EntityNotFoundException("Гость %s не найден".formatted(guestId));
        }
        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    /**
     * Итог выполненного приказа записывают после хода: мастер или тот, кто отдал приказ.
     */
    @Transactional
    public PlayerBastionResponse writeOrderResult(UUID bastionId, UUID orderId, ActivityRequests.OrderResult request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        PlayerBastionAccess.requireNotArchived(bastion);

        PlayerBastionOrder order = orderRepository.findById(orderId)
                .filter(item -> item.getBastionId().equals(bastionId))
                .orElseThrow(() -> new EntityNotFoundException("Приказ %s не найден".formatted(orderId)));
        if (!viewer.manager() && !order.getGivenBy().equals(userId)) {
            throw forbidden("Итог приказа записывает тот, кто его отдал, или мастер");
        }
        if (order.getStatus() != OrderStatus.COMPLETED || order.getOrder().isBastionWide()) {
            throw conflict("Итог записывается у выполненного приказа сооружению");
        }

        order.setResult(trimToNull(request.result()));
        orderRepository.save(order);
        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    /** Отмена приказа в тот же ход — цена возвращается в казну. */
    @Transactional
    public PlayerBastionResponse cancelOrder(UUID bastionId, UUID orderId) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        requireActive(bastion);

        PlayerBastionOrder order = orderRepository.findById(orderId)
                .filter(item -> item.getBastionId().equals(bastionId))
                .orElseThrow(() -> new EntityNotFoundException("Приказ %s не найден".formatted(orderId)));
        if (!viewer.manager() && !order.getGivenBy().equals(userId)) {
            throw forbidden("Отменить приказ может тот, кто его отдал, или мастер");
        }
        if (order.getStatus() != OrderStatus.ACTIVE || order.getGivenOnTurn() != bastion.getTurn()) {
            throw conflict("Отменить можно только приказ, отданный в текущий ход");
        }
        cancel(bastion, order, userId);
        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    /**
     * «Обслуживать» отдаётся вместо остальных приказов: те, что сооружения ещё выполняют,
     * отменяются, если отдающий это подтвердил. Чужие приказы снимает только мастер.
     */
    private void cancelForMaintain(PlayerBastion bastion, UUID userId, boolean master, boolean confirmed) {
        List<PlayerBastionOrder> active = orderRepository.findAllByBastionIdAndStatus(bastion.getId(),
                OrderStatus.ACTIVE);
        if (active.isEmpty()) {
            return;
        }
        if (!confirmed) {
            throw conflict("Сооружения ещё выполняют приказы: «Обслуживать» отменит их");
        }
        if (!master && active.stream().anyMatch(item -> !item.getGivenBy().equals(userId))) {
            throw forbidden("Чужие приказы отменит мастер или тот, кто их отдал");
        }
        active.forEach(item -> cancel(bastion, item, userId));
    }

    /** Отменяет приказ; цена приказа, отданного в этот ход, возвращается в казну. */
    private void cancel(PlayerBastion bastion, PlayerBastionOrder order, UUID userId) {
        order.setStatus(OrderStatus.CANCELLED);
        if (order.getCostGp() > 0 && order.getGivenOnTurn() == bastion.getTurn()) {
            bastion.setTreasuryGp(bastion.getTreasuryGp() + order.getCostGp());
            record(bastion, order.getCostGp(), Reason.REFUND,
                    "Отмена приказа «%s»".formatted(order.getOrder().getName()), userId);
        }
        orderRepository.save(order);
    }

    // ------------------------------------------------------------------ ход

    /**
     * Ход бастиона: проходят 7 дней. Достраиваются сооружения и расширения, завершаются
     * приказы со сроком, запись попадает в журнал ходов. Если в ход не отдали приказов
     * или отдали «Обслуживать», ход отмечается обслуживанием, и мастер записывает событие.
     */
    @Transactional
    public PlayerBastionResponse performTurn(UUID bastionId, ActivityRequests.Turn request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireManager(bastion, userId);
        requireActive(bastion);

        int next = bastion.getTurn() + 1;
        List<String> summary = new ArrayList<>();
        Map<UUID, PlayerBastionFacility> facilities = facilitiesById(bastion);
        Map<String, BastionFacility> reference = loadReference(facilities.values().stream()
                .map(PlayerBastionFacility::getFacilityUrl));

        for (PlayerBastionFacility facility : facilities.values()) {
            String name = facilityName(facility, reference);
            if (facility.getStatus() == FacilityStatus.BUILDING
                    && facility.getReadyOnTurn() != null && facility.getReadyOnTurn() <= next) {
                facility.setStatus(FacilityStatus.READY);
                facility.setReadyOnTurn(null);
                summary.add("Достроено: %s (%s)".formatted(name, facility.getMember().getCharacterName()));
            }
            if (facility.getPendingSpace() != null
                    && facility.getPendingReadyOnTurn() != null && facility.getPendingReadyOnTurn() <= next) {
                facility.setSpace(facility.getPendingSpace());
                facility.setPendingSpace(null);
                facility.setPendingReadyOnTurn(null);
                summary.add("Расширено: %s — теперь %s".formatted(name, facility.getSpace().getName().toLowerCase()));
            }
        }

        if (bastion.getDefenseWallSquaresBuilding() > 0
                && bastion.getDefenseWallReadyOnTurn() != null && bastion.getDefenseWallReadyOnTurn() <= next) {
            summary.add("Достроена защитная стена: %d бл., всего %d".formatted(bastion.getDefenseWallSquaresBuilding(),
                    bastion.getDefenseWallSquares() + bastion.getDefenseWallSquaresBuilding()));
            bastion.setDefenseWallSquares(bastion.getDefenseWallSquares() + bastion.getDefenseWallSquaresBuilding());
            bastion.setDefenseWallSquaresBuilding(0);
            bastion.setDefenseWallReadyOnTurn(null);
        }

        List<PlayerBastionOrder> givenThisTurn = orderRepository.findAllByBastionIdAndGivenOnTurnAndStatusNot(
                bastionId, bastion.getTurn(), OrderStatus.CANCELLED);
        boolean maintain = givenThisTurn.isEmpty()
                || givenThisTurn.stream().anyMatch(order -> order.getOrder().isBastionWide());

        // Ход без приказов — тоже обслуживание: событие бросается, если его ещё не было
        if (maintain && !events.rolledOnTurn(bastion, bastion.getTurn())) {
            events.rollForMaintain(bastion, reference, userId);
        }
        summary.addAll(events.departures(bastion, next));

        Map<UUID, String> results = Optional.ofNullable(request.results()).orElse(Map.of());
        for (PlayerBastionOrder order : orderRepository.findAllByBastionIdAndStatus(bastionId, OrderStatus.ACTIVE)) {
            if (order.getCompletesOnTurn() > next) {
                continue;
            }
            order.setStatus(OrderStatus.COMPLETED);
            order.setResult(trimToNull(results.get(order.getId())));
            Optional<PlayerBastionFacility> orderFacility = Optional.ofNullable(order.getFacilityId())
                    .map(facilities::get);
            String target = orderFacility
                    .map(facility -> facilityName(facility, reference))
                    .orElse("бастион");
            String recruited = orderFacility
                    .filter(facility -> isRecruitDefenders(reference.get(facility.getFacilityUrl()), order.getOrder()))
                    .map(PlayerBastionActivityService::recruitDefenders)
                    .or(() -> orderFacility
                            .filter(facility -> order.getCreature() != null)
                            .map(facility -> settleCreature(facility, order.getCreature())))
                    .or(() -> orderFacility
                            .filter(facility -> order.getAnimal() != null)
                            .map(facility -> order.isAnimalSale()
                                    ? sellAnimal(bastion, facility, order.getAnimal(), userId)
                                    : settleAnimal(facility, order.getAnimal(), next)))
                    .orElse("");
            summary.add("Выполнен приказ «%s» — %s%s%s%s".formatted(order.getOrder().getName(), target,
                    Optional.ofNullable(order.getOptionName())
                            .filter(option -> order.getCreature() == null && order.getAnimal() == null)
                            .map(option -> ": " + option)
                            .orElse(""),
                    recruited,
                    Optional.ofNullable(order.getResult()).map(result -> ". " + result).orElse("")));
            orderRepository.save(order);
        }

        PlayerBastionTurn turn = new PlayerBastionTurn();
        turn.setBastionId(bastionId);
        turn.setNumber(next);
        turn.setMaintain(maintain);
        turn.setEvent(trimToNull(request.event()));
        turn.setSummary(summary);
        turn.setPerformedBy(userId);
        turnRepository.save(turn);

        bastion.setTurn(next);
        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    // ------------------------------------------------------------------ казна

    @Transactional
    public PlayerBastionResponse adjustTreasury(UUID bastionId, ActivityRequests.Treasury request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireManager(bastion, userId);
        PlayerBastionAccess.requireNotArchived(bastion);

        long amount = request.amountGp();
        if (amount == 0) {
            throw badRequest("Сумма должна быть больше или меньше нуля");
        }
        if (bastion.getTreasuryGp() + amount < 0) {
            throw conflict("В казне %d зм — столько не списать".formatted(bastion.getTreasuryGp()));
        }
        bastion.setTreasuryGp(bastion.getTreasuryGp() + amount);
        record(bastion, amount, amount > 0 ? Reason.DEPOSIT : Reason.WITHDRAWAL, trimToNull(request.note()), userId);
        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    // ------------------------------------------------------------------ стройка

    /** Новое базовое сооружение за деньги и время из правил (тесное — 500 зм и 20 дней). */
    @Transactional
    public PlayerBastionResponse buildBasic(UUID bastionId, UUID memberId, ActivityRequests.BuildBasic request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        requireActive(bastion);
        PlayerBastionMember member = findMember(bastion, memberId);
        requireOwnerOrMaster(member, userId, viewer.manager(),
                "Строит сооружения игрок этого персонажа или мастер");

        BastionFacility reference = loadReference(Stream.of(request.facilityUrl())).get(request.facilityUrl());
        if (reference == null || reference.isHiddenEntity() || reference.getCategory() != FacilityCategory.BASIC) {
            throw badRequest("Построить можно только базовое сооружение из справочника");
        }
        FacilitySpace space = request.space();
        charge(bastion, space.getBuildCost(), Reason.BUILD,
                "Постройка: %s (%s)".formatted(reference.getName(), space.getName().toLowerCase()), userId);

        PlayerBastionFacility facility = new PlayerBastionFacility();
        facility.setMember(member);
        facility.setFacilityUrl(reference.getUrl());
        facility.setSpace(space);
        facility.setPrerequisiteConfirmed(true);
        facility.setStatus(FacilityStatus.BUILDING);
        facility.setReadyOnTurn(bastion.getTurn() + turnsFor(space.getBuildDays()));
        member.getFacilities().add(facility);

        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    /**
     * Блоки защитной стены: каждый 5-футовый квадрат стоит 250 зм и строится 10 дней, блоки
     * строятся друг за другом. Строит тот, кто может править бастион; пока партия в стройке,
     * новую не начинают.
     */
    @Transactional
    public PlayerBastionResponse buildWall(UUID bastionId, ActivityRequests.BuildWall request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        if (!viewer.canEdit()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Строит стены тот, кто может править бастион");
        }
        requireActive(bastion);
        if (bastion.getDefenseWallSquaresBuilding() > 0) {
            throw conflict("Стена уже строится — новые блоки можно заказать, когда она достроится к ходу %d"
                    .formatted(bastion.getDefenseWallReadyOnTurn()));
        }

        int squares = request.squares();
        charge(bastion, (long) squares * BastionRules.DEFENSIVE_WALL_COST_PER_SQUARE, Reason.WALL,
                "Защитная стена: %d бл.".formatted(squares), userId);
        bastion.setDefenseWallSquaresBuilding(squares);
        bastion.setDefenseWallReadyOnTurn(bastion.getTurn()
                + turnsFor(squares * BastionRules.DEFENSIVE_WALL_DAYS_PER_SQUARE));
        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    /**
     * Расширение: базовое — до следующего пространства по цене правил, специализированное —
     * по цене его описания. Пока идёт расширение, сооружение работает в прежнем пространстве.
     */
    @Transactional
    public PlayerBastionResponse enlarge(UUID bastionId, UUID facilityId) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        requireActive(bastion);
        PlayerBastionFacility facility = findFacility(bastion, facilityId);
        requireOwnerOrMaster(facility, userId, viewer.manager(),
                "Расширяет сооружение игрок этого персонажа или мастер");
        if (!facility.isReady() || facility.getPendingSpace() != null) {
            throw conflict("Сооружение ещё строится или уже расширяется");
        }

        BastionFacility reference = referenceOf(facility);
        FacilitySpace target;
        long cost;
        Integer days;
        if (reference.getCategory() == FacilityCategory.BASIC) {
            target = facility.getSpace().next();
            if (target == null) {
                throw conflict("Просторное сооружение расширять некуда");
            }
            cost = facility.getSpace().getEnlargeCost();
            days = facility.getSpace().getEnlargeDays();
        } else {
            FacilityEnlargement enlargement = reference.getEnlargement();
            if (enlargement == null || enlargement.getSpace() == null || enlargement.getSpace() == facility.getSpace()) {
                throw conflict("«%s» не расширяется или уже расширено".formatted(reference.getName()));
            }
            target = enlargement.getSpace();
            cost = Optional.ofNullable(enlargement.getCost()).orElse(0);
            days = enlargement.getDays();
        }

        charge(bastion, cost, Reason.ENLARGE, "Расширение: %s".formatted(reference.getName()), userId);
        facility.setPendingSpace(target);
        facility.setPendingReadyOnTurn(bastion.getTurn() + turnsFor(days));
        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    /** Новое специализированное сооружение, полученное с уровнем, — сразу и бесплатно. */
    @Transactional
    public PlayerBastionResponse addSpecial(UUID bastionId, UUID memberId, ActivityRequests.AddSpecial request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        requireActive(bastion);
        PlayerBastionMember member = findMember(bastion, memberId);
        requireOwnerOrMaster(member, userId, viewer.manager(),
                "Сооружения персонажа выбирает его игрок или мастер");

        Map<String, BastionFacility> reference = loadReference(Stream.concat(
                member.getFacilities().stream().map(PlayerBastionFacility::getFacilityUrl),
                Stream.of(request.facilityUrl())));
        List<FacilitySetupRequest.Special> special = new ArrayList<>(member.getFacilities().stream()
                .filter(facility -> Optional.ofNullable(reference.get(facility.getFacilityUrl()))
                        .map(BastionFacility::getCategory)
                        .orElse(null) == FacilityCategory.SPECIAL)
                .map(facility -> new FacilitySetupRequest.Special(facility.getFacilityUrl(), facility.getChoices()))
                .toList());
        special.add(new FacilitySetupRequest.Special(request.facilityUrl(), request.choices()));
        PlayerBastionSetupRules.validateSpecial(member.getCharacterLevel(), special, reference, choiceOptions.resolver());

        BastionFacility added = reference.get(request.facilityUrl());
        PlayerBastionFacility facility = new PlayerBastionFacility();
        facility.setMember(member);
        facility.setFacilityUrl(added.getUrl());
        facility.setSpace(added.getSpace());
        facility.setChoices(new ArrayList<>(Optional.ofNullable(request.choices()).orElse(List.of())));
        facility.setPrerequisiteConfirmed(added.getPrerequisite() == null);
        if (PlayerBastionResponseMapper.isStable(added)) {
            facility.setAnimals(StableAnimal.starting(bastion.getTurn()));
        }
        member.getFacilities().add(facility);

        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    /**
     * Убирает сооружение. Пока бастион в закладке, ошибочно выбранное сооружение убирает игрок
     * этого персонажа или тот, кто ведёт бастион. В запущенном бастионе — только тот, кто ведёт:
     * так делается замена специализированного сооружения при повышении уровня. Незавершённые
     * приказы сооружения отменяются без возврата денег: работа уже шла. Клетки сооружения
     * исчезают с плана сами.
     */
    @Transactional
    public PlayerBastionResponse removeFacility(UUID bastionId, UUID facilityId) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        PlayerBastionAccess.requireNotArchived(bastion);
        BastionViewer viewer = bastion.getStatus() == PlayerBastionStatus.SETUP
                ? access.requireViewer(bastion, userId)
                : access.requireManager(bastion, userId);

        PlayerBastionFacility facility = findFacility(bastion, facilityId);
        requireOwnerOrMaster(facility, userId, viewer.manager(),
                "Сооружение убирает игрок этого персонажа или тот, кто ведёт бастион");
        orderRepository.findAllByBastionIdAndStatus(bastionId, OrderStatus.ACTIVE).stream()
                .filter(order -> facilityId.equals(order.getFacilityId()))
                .forEach(order -> {
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                });
        facility.getMember().getFacilities().remove(facility);
        return mapper.toResponse(saveWithNewVersion(bastion), viewer);
    }

    // ------------------------------------------------------------------ помощники

    private static void validateFacilityOrder(PlayerBastionFacility facility, BastionFacility reference, BastionOrder order) {
        if (!facility.isReady()) {
            throw conflict("«%s» ещё строится".formatted(reference.getName()));
        }
        if (!facility.isPrerequisiteConfirmed()) {
            throw conflict("Мастер ещё не подтвердил требование «%s»".formatted(reference.getName()));
        }
        if (!Optional.ofNullable(reference.getOrders()).orElse(List.of()).contains(order)) {
            throw badRequest("«%s» не принимает приказ «%s»".formatted(reference.getName(), order.getName()));
        }
    }

    /** Приказ «Завербовать» бараку: нанимает защитников бастиона. */
    private static boolean isRecruitDefenders(BastionFacility reference, BastionOrder order) {
        return order == BastionOrder.RECRUIT && PlayerBastionResponseMapper.isBarrack(reference);
    }

    /**
     * Барак нанимает защитников, сколько поместится.
     *
     * @return приписка к записи хода: сколько нанято и сколько стало
     */
    private static String recruitDefenders(PlayerBastionFacility barrack) {
        int capacity = BastionRules.barrackCapacity(barrack.getSpace());
        int hired = Math.max(0, Math.min(BastionRules.BARRACK_DEFENDERS_PER_RECRUIT,
                capacity - barrack.getDefenders()));
        barrack.setDefenders(barrack.getDefenders() + hired);
        return ", защитников +%d (%d из %d)".formatted(hired, barrack.getDefenders(), capacity);
    }

    /** Приказ «Завербовать» зверинцу: в вольеры добавляется существо. */
    private static boolean isRecruitCreature(BastionFacility reference, BastionOrder order) {
        return order == BastionOrder.RECRUIT && PlayerBastionResponseMapper.isMenagerie(reference);
    }

    /**
     * Существо, которое зверинец вербует по приказу: из таблицы зверинца или, если приказ
     * отдаёт Мастер, любое другое с ценой по показателю опасности. В вольерах должно
     * хватать места.
     *
     * @return существо; {@code null}, если приказ не вербует существо
     */
    private static MenagerieCreature recruitCreature(PlayerBastionFacility facility,
                                                     BastionFacility reference,
                                                     ActivityRequests.GiveOrder request,
                                                     boolean master) {
        ActivityRequests.RecruitCreature wanted = request.creature();
        if (!isRecruitCreature(reference, request.order())) {
            if (wanted != null) {
                throw badRequest("Существо вербует только зверинец");
            }
            return null;
        }
        if (wanted == null) {
            throw badRequest("Выберите существо для зверинца");
        }

        MenagerieCreature creature = new MenagerieCreature();
        creature.setId(UUID.randomUUID().toString());
        if (wanted.beast() != null) {
            creature.setBeast(wanted.beast());
            creature.setName(wanted.beast().getName());
            creature.setSize(wanted.beast().getSize());
            creature.setCost(wanted.beast().getCost());
        } else {
            if (!master) {
                throw forbidden("Существо не из таблицы зверинца добавляется с согласия Мастера: приказ отдаёт Мастер");
            }
            String name = trimToNull(wanted.name());
            if (name == null) {
                throw badRequest("Укажите, какое существо вербовать");
            }
            if (BastionRules.menagerieSpace(wanted.size()) == 0) {
                throw badRequest("В вольерах держат существ не больше большого размера");
            }
            creature.setName(name);
            creature.setSize(wanted.size());
            creature.setCost(BastionRules.menagerieChallengeCost(wanted.challenge())
                    .orElseThrow(() -> badRequest("Зверинец принимает существ с ПО от 0 до 3")));
            creature.setChallenge(wanted.challenge().trim());
        }

        int occupied = PlayerBastionResponseMapper.menagerieOccupied(facility);
        if (occupied + BastionRules.menagerieSpace(creature.getSize()) > BastionRules.MENAGERIE_CAPACITY) {
            throw conflict("В вольерах «%s» нет места для существа «%s»".formatted(reference.getName(),
                    creature.getName()));
        }
        return creature;
    }

    /**
     * Завербованное существо поселяется в вольерах, если там ещё есть место.
     *
     * @return приписка к записи хода
     */
    private static String settleCreature(PlayerBastionFacility menagerie, MenagerieCreature creature) {
        int space = BastionRules.menagerieSpace(creature.getSize());
        if (PlayerBastionResponseMapper.menagerieOccupied(menagerie) + space > BastionRules.MENAGERIE_CAPACITY) {
            return ": %s — в вольерах нет места".formatted(creature.getName());
        }
        menagerie.getCreatures().add(creature);
        return ": %s поселяется в вольерах".formatted(creature.getName());
    }

    /** Приказ «Торговать» конюшне: покупка или продажа животного. */
    private static boolean isStableTrade(BastionFacility reference, BastionOrder order) {
        return order == BastionOrder.TRADE && PlayerBastionResponseMapper.isStable(reference);
    }

    /**
     * Животное, которое конюшня покупает или продаёт по приказу. Продают животное, которое
     * живёт в конюшне; покупают из таблицы или, если приказ отдаёт Мастер, любое другое по
     * указанной цене. Для покупки в конюшне должно хватать места.
     *
     * @return животное; {@code null}, если приказ не торгует животными
     */
    private static StableAnimal tradeAnimal(PlayerBastionFacility facility,
                                            BastionFacility reference,
                                            ActivityRequests.GiveOrder request,
                                            boolean master) {
        ActivityRequests.StableTrade wanted = request.trade();
        if (!isStableTrade(reference, request.order())) {
            if (wanted != null) {
                throw badRequest("Животными торгует только конюшня");
            }
            return null;
        }
        if (wanted == null) {
            throw badRequest("Выберите, какое животное купить или продать");
        }

        String sellId = trimToNull(wanted.sellAnimalId());
        if (sellId != null) {
            return findAnimal(facility, sellId);
        }

        StableAnimal animal;
        if (wanted.kind() != null) {
            animal = StableAnimal.of(wanted.kind(), 0);
        } else {
            if (!master) {
                throw forbidden("Животное не из таблицы покупается с согласия Мастера: приказ отдаёт Мастер");
            }
            String name = trimToNull(wanted.name());
            if (name == null) {
                throw badRequest("Укажите, какое животное купить");
            }
            if (BastionRules.stableSpace(wanted.size()) == 0) {
                throw badRequest("В конюшне держат животных не больше большого размера");
            }
            if (wanted.cost() == null) {
                throw badRequest("Укажите цену животного");
            }
            animal = new StableAnimal();
            animal.setId(UUID.randomUUID().toString());
            animal.setName(name);
            animal.setSize(wanted.size());
            animal.setCost(wanted.cost());
        }

        if (PlayerBastionResponseMapper.stableOccupied(facility) + BastionRules.stableSpace(animal.getSize())
                > BastionRules.STABLE_CAPACITY) {
            throw conflict("В «%s» нет места для животного «%s»".formatted(reference.getName(), animal.getName()));
        }
        return animal;
    }

    private static StableAnimal findAnimal(PlayerBastionFacility facility, String animalId) {
        return facility.getAnimals().stream()
                .filter(animal -> animalId.equals(animal.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Животное %s не найдено".formatted(animalId)));
    }

    /**
     * Купленное животное поселяется в конюшне, если там ещё есть место.
     *
     * @return приписка к записи хода
     */
    private static String settleAnimal(PlayerBastionFacility stable, StableAnimal animal, int turn) {
        if (PlayerBastionResponseMapper.stableOccupied(stable) + BastionRules.stableSpace(animal.getSize())
                > BastionRules.STABLE_CAPACITY) {
            return ": %s — в конюшне нет места".formatted(animal.getName());
        }
        animal.setArrivedOnTurn(turn);
        stable.getAnimals().add(animal);
        return ": куплено животное «%s»".formatted(animal.getName());
    }

    /**
     * Проданное животное уходит из конюшни, покупатель платит обычную цену с наценкой:
     * 20%, с 13 уровня персонажа — 50%.
     *
     * @return приписка к записи хода
     */
    private String sellAnimal(PlayerBastion bastion, PlayerBastionFacility stable, StableAnimal sold, UUID userId) {
        Optional<StableAnimal> animal = stable.getAnimals().stream()
                .filter(item -> item.getId().equals(sold.getId()))
                .findFirst();
        if (animal.isEmpty()) {
            return ": животного «%s» уже нет в конюшне".formatted(sold.getName());
        }
        stable.getAnimals().remove(animal.get());
        long price = BastionRules.stableSalePrice(animal.get().getCost(), stable.getMember().getCharacterLevel());
        bastion.setTreasuryGp(bastion.getTreasuryGp() + price);
        record(bastion, price, Reason.SALE, "Продажа: %s".formatted(animal.get().getName()), userId);
        return ": продано животное «%s» за %d зм".formatted(animal.get().getName(), price);
    }

    /**
     * Предмет, который мастерская изготовит по приказу: только из тех, что создаются
     * выбранными в ней инструментами.
     */
    private Optional<PlayerBastionResponse.CraftableItem> craftItem(PlayerBastionFacility facility,
                                                                    BastionFacility reference,
                                                                    ActivityRequests.GiveOrder request) {
        String url = trimToNull(request.itemUrl());
        if (url == null) {
            return Optional.empty();
        }
        if (request.order() != BastionOrder.CRAFT) {
            throw badRequest("Предмет выбирается только для приказа «%s»".formatted(BastionOrder.CRAFT.getName()));
        }
        PlayerBastionResponse.CraftableItem item = Optional.ofNullable(mapper.craftableItems(facility, reference))
                .orElse(List.of())
                .stream()
                .filter(craft -> url.equals(craft.url()))
                .findFirst()
                .orElseThrow(() -> badRequest("«%s» не изготавливает предмет «%s» выбранными инструментами"
                        .formatted(reference.getName(), url)));
        return Optional.of(item);
    }

    private static Optional<FacilityOrderOption> findOption(BastionFacility reference, BastionOrder order, String name) {
        if (!StringUtils.hasText(name)) {
            return Optional.empty();
        }
        return Optional.ofNullable(reference.getOrderOptions()).orElse(List.of()).stream()
                .filter(option -> option.getOrder() == order && name.trim().equals(option.getName()))
                .findFirst();
    }

    /** Списывает цену из казны; денег не хватает — 409 до любых изменений. */
    private void charge(PlayerBastion bastion, long cost, Reason reason, String note, UUID userId) {
        if (cost <= 0) {
            return;
        }
        if (bastion.getTreasuryGp() < cost) {
            throw conflict("В казне %d зм, а нужно %d зм".formatted(bastion.getTreasuryGp(), cost));
        }
        bastion.setTreasuryGp(bastion.getTreasuryGp() - cost);
        record(bastion, -cost, reason, note, userId);
    }

    private void record(PlayerBastion bastion, long amount, Reason reason, String note, UUID userId) {
        PlayerBastionLedgerEntry entry = new PlayerBastionLedgerEntry();
        entry.setBastionId(bastion.getId());
        entry.setTurn(bastion.getTurn());
        entry.setAmountGp(amount);
        entry.setReason(reason);
        entry.setNote(note);
        entry.setCreatedBy(userId);
        ledgerRepository.save(entry);
    }

    /**
     * Сохраняет бастион с новой версией: правки сооружений и журналов сами версию
     * бастиона не поднимают, а параллельная правка должна получить конфликт.
     */
    private PlayerBastion saveWithNewVersion(PlayerBastion bastion) {
        entityManager.lock(bastion, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        return bastionRepository.saveAndFlush(bastion);
    }

    private static void requireActive(PlayerBastion bastion) {
        if (bastion.getStatus() != PlayerBastionStatus.ACTIVE) {
            throw conflict("Это делается, когда бастион запущен и не в архиве");
        }
    }

    private static void requireOwnerOrMaster(PlayerBastionFacility facility, UUID userId, boolean master, String message) {
        requireOwnerOrMaster(facility.getMember(), userId, master, message);
    }

    private static void requireOwnerOrMaster(PlayerBastionMember member, UUID userId, boolean master, String message) {
        if (!master && !member.getUserId().equals(userId)) {
            throw forbidden(message);
        }
    }

    private static boolean isMember(PlayerBastion bastion, UUID userId) {
        return bastion.getMembers().stream().anyMatch(member -> member.getUserId().equals(userId));
    }

    /**
     * Персонаж каждого игрока бастиона: по нему журнал подписывает приказы и движения казны.
     * У игрока с несколькими персонажами берётся первый.
     */
    private static Map<UUID, String> charactersByUser(PlayerBastion bastion) {
        return bastion.getMembers().stream()
                .collect(Collectors.toMap(PlayerBastionMember::getUserId, PlayerBastionMember::getCharacterName,
                        (first, second) -> first));
    }

    private static Map<UUID, PlayerBastionFacility> facilitiesById(PlayerBastion bastion) {
        return bastion.getMembers().stream()
                .flatMap(member -> member.getFacilities().stream())
                .collect(Collectors.toMap(PlayerBastionFacility::getId, Function.identity()));
    }

    private static PlayerBastionFacility findFacility(PlayerBastion bastion, UUID facilityId) {
        if (facilityId == null) {
            throw badRequest("Укажите сооружение");
        }
        return Optional.ofNullable(facilitiesById(bastion).get(facilityId))
                .orElseThrow(() -> new EntityNotFoundException("Сооружение %s не найдено".formatted(facilityId)));
    }

    private static PlayerBastionMember findMember(PlayerBastion bastion, UUID memberId) {
        return bastion.getMembers().stream()
                .filter(member -> member.getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Персонаж %s в бастионе не найден".formatted(memberId)));
    }

    private BastionFacility referenceOf(PlayerBastionFacility facility) {
        return facilityRepository.findById(facility.getFacilityUrl())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Сооружения %s нет в справочнике".formatted(facility.getFacilityUrl())));
    }

    private Map<String, BastionFacility> allReference(PlayerBastion bastion) {
        return loadReference(facilitiesById(bastion).values().stream().map(PlayerBastionFacility::getFacilityUrl));
    }

    private Map<String, BastionFacility> loadReference(Stream<String> urls) {
        Collection<String> distinct = urls.filter(Objects::nonNull).collect(Collectors.toSet());
        if (distinct.isEmpty()) {
            return Map.of();
        }
        return facilityRepository.findAllById(distinct).stream()
                .collect(Collectors.toMap(BastionFacility::getUrl, Function.identity()));
    }

    private static String facilityName(PlayerBastionFacility facility, Map<String, BastionFacility> reference) {
        return Optional.ofNullable(reference.get(facility.getFacilityUrl()))
                .map(BastionFacility::getName)
                .orElse(facility.getFacilityUrl());
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    private static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }

    private static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message);
    }
}
