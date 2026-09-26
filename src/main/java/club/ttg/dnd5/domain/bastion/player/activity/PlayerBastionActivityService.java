package club.ttg.dnd5.domain.bastion.player.activity;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilityEnlargement;
import club.ttg.dnd5.domain.bastion.model.FacilityOrderOption;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.activity.PlayerBastionLedgerEntry.Reason;
import club.ttg.dnd5.domain.bastion.player.activity.PlayerBastionOrder.OrderStatus;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembership;
import club.ttg.dnd5.domain.bastion.player.client.GameRole;
import club.ttg.dnd5.domain.bastion.player.model.FacilityStatus;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionFacility;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionMember;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionRepository;
import club.ttg.dnd5.domain.bastion.player.rest.dto.FacilitySetupRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionResponse;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionAccess;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionResponseMapper;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionSetupRules;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionLabel;
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
 * Жизнь запущенного бастиона: приказы, ходы, казна, стройка и расширение.
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
        GameMembership membership = access.requireParticipant(bastion.getGameId(), userId);
        boolean master = membership.role() == GameRole.MASTER;

        Map<UUID, PlayerBastionFacility> facilities = facilitiesById(bastion);
        Map<String, BastionFacility> reference = loadReference(facilities.values().stream()
                .map(PlayerBastionFacility::getFacilityUrl));
        PageRequest page = PageRequest.of(0, JOURNAL_LIMIT);

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
                        order.getCostGp(),
                        order.getGivenOnTurn(),
                        order.getCompletesOnTurn(),
                        order.getStatus(),
                        order.getGivenBy(),
                        order.getNote(),
                        order.getResult(),
                        order.getStatus() == OrderStatus.ACTIVE
                                && order.getGivenOnTurn() == bastion.getTurn()
                                && bastion.getStatus() == PlayerBastionStatus.ACTIVE
                                && (master || order.getGivenBy().equals(userId))))
                .toList();

        List<ActivityResponse.Turn> turns = turnRepository.findAllByBastionIdOrderByNumberDesc(bastionId, page).stream()
                .map(turn -> new ActivityResponse.Turn(turn.getNumber(), turn.isMaintain(), turn.getEvent(),
                        turn.getSummary(), turn.getPerformedBy(), turn.getPerformedAt()))
                .toList();

        List<ActivityResponse.LedgerEntry> ledger = ledgerRepository.findAllByBastionIdOrderByCreatedAtDesc(bastionId, page)
                .stream()
                .map(entry -> new ActivityResponse.LedgerEntry(entry.getTurn(), entry.getAmountGp(), entry.getReason(),
                        entry.getNote(), entry.getCreatedBy(), entry.getCreatedAt()))
                .toList();

        return new ActivityResponse(orders, turns, ledger);
    }

    // ------------------------------------------------------------------ приказы

    @Transactional
    public PlayerBastionResponse giveOrder(UUID bastionId, ActivityRequests.GiveOrder request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        GameMembership membership = access.requireParticipant(bastion.getGameId(), userId);
        boolean master = membership.role() == GameRole.MASTER;
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
            if (!givenThisTurn.isEmpty()) {
                throw conflict("В этот ход уже отданы приказы: «Обслуживать» отдаётся вместо них");
            }
            order.setCompletesOnTurn(bastion.getTurn() + 1);
        } else {
            PlayerBastionFacility facility = findFacility(bastion, request.facilityId());
            requireOwnerOrMaster(facility, userId, master, "Приказы сооружению отдаёт игрок этого персонажа или мастер");
            if (givenThisTurn.stream().anyMatch(item -> item.getOrder().isBastionWide())) {
                throw conflict("В этот ход бастион обслуживают: другие приказы отдать нельзя");
            }
            BastionFacility reference = referenceOf(facility);
            validateFacilityOrder(facility, reference, request.order());
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

            long cost = option.map(FacilityOrderOption::getCost).orElse(0);
            order.setFacilityId(facility.getId());
            order.setOptionName(option.map(FacilityOrderOption::getName).orElse(null));
            order.setCostGp(cost);
            order.setCompletesOnTurn(bastion.getTurn() + turnsFor(option.map(FacilityOrderOption::getDays).orElse(null)));
            charge(bastion, cost, Reason.ORDER, "Приказ «%s» — %s".formatted(request.order().getName(),
                    reference.getName()), userId);
        }

        orderRepository.save(order);
        return mapper.toResponse(saveWithNewVersion(bastion), membership, userId);
    }

    /** Отмена приказа в тот же ход — цена возвращается в казну. */
    @Transactional
    public PlayerBastionResponse cancelOrder(UUID bastionId, UUID orderId) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        GameMembership membership = access.requireParticipant(bastion.getGameId(), userId);
        requireActive(bastion);

        PlayerBastionOrder order = orderRepository.findById(orderId)
                .filter(item -> item.getBastionId().equals(bastionId))
                .orElseThrow(() -> new EntityNotFoundException("Приказ %s не найден".formatted(orderId)));
        if (membership.role() != GameRole.MASTER && !order.getGivenBy().equals(userId)) {
            throw forbidden("Отменить приказ может тот, кто его отдал, или мастер");
        }
        if (order.getStatus() != OrderStatus.ACTIVE || order.getGivenOnTurn() != bastion.getTurn()) {
            throw conflict("Отменить можно только приказ, отданный в текущий ход");
        }

        order.setStatus(OrderStatus.CANCELLED);
        if (order.getCostGp() > 0) {
            bastion.setTreasuryGp(bastion.getTreasuryGp() + order.getCostGp());
            record(bastion, order.getCostGp(), Reason.REFUND,
                    "Отмена приказа «%s»".formatted(order.getOrder().getName()), userId);
        }
        orderRepository.save(order);
        return mapper.toResponse(saveWithNewVersion(bastion), membership, userId);
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
        GameMembership membership = access.requireMaster(bastion.getGameId(), userId);
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

        List<PlayerBastionOrder> givenThisTurn = orderRepository.findAllByBastionIdAndGivenOnTurnAndStatusNot(
                bastionId, bastion.getTurn(), OrderStatus.CANCELLED);
        boolean maintain = givenThisTurn.isEmpty()
                || givenThisTurn.stream().anyMatch(order -> order.getOrder().isBastionWide());

        Map<UUID, String> results = Optional.ofNullable(request.results()).orElse(Map.of());
        for (PlayerBastionOrder order : orderRepository.findAllByBastionIdAndStatus(bastionId, OrderStatus.ACTIVE)) {
            if (order.getCompletesOnTurn() > next) {
                continue;
            }
            order.setStatus(OrderStatus.COMPLETED);
            order.setResult(trimToNull(results.get(order.getId())));
            String target = Optional.ofNullable(order.getFacilityId())
                    .map(facilities::get)
                    .map(facility -> facilityName(facility, reference))
                    .orElse("бастион");
            summary.add("Выполнен приказ «%s» — %s%s%s".formatted(order.getOrder().getName(), target,
                    Optional.ofNullable(order.getOptionName()).map(option -> ": " + option).orElse(""),
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
        return mapper.toResponse(saveWithNewVersion(bastion), membership, userId);
    }

    // ------------------------------------------------------------------ казна

    @Transactional
    public PlayerBastionResponse adjustTreasury(UUID bastionId, ActivityRequests.Treasury request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        GameMembership membership = access.requireMaster(bastion.getGameId(), userId);
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
        return mapper.toResponse(saveWithNewVersion(bastion), membership, userId);
    }

    // ------------------------------------------------------------------ стройка

    /** Новое базовое сооружение за деньги и время из правил (тесное — 500 зм и 20 дней). */
    @Transactional
    public PlayerBastionResponse buildBasic(UUID bastionId, UUID memberId, ActivityRequests.BuildBasic request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        GameMembership membership = access.requireParticipant(bastion.getGameId(), userId);
        requireActive(bastion);
        PlayerBastionMember member = findMember(bastion, memberId);
        requireOwnerOrMaster(member, userId, membership.role() == GameRole.MASTER,
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

        return mapper.toResponse(saveWithNewVersion(bastion), membership, userId);
    }

    /**
     * Расширение: базовое — до следующего пространства по цене правил, специализированное —
     * по цене его описания. Пока идёт расширение, сооружение работает в прежнем пространстве.
     */
    @Transactional
    public PlayerBastionResponse enlarge(UUID bastionId, UUID facilityId) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        GameMembership membership = access.requireParticipant(bastion.getGameId(), userId);
        requireActive(bastion);
        PlayerBastionFacility facility = findFacility(bastion, facilityId);
        requireOwnerOrMaster(facility, userId, membership.role() == GameRole.MASTER,
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
        return mapper.toResponse(saveWithNewVersion(bastion), membership, userId);
    }

    /** Новое специализированное сооружение, полученное с уровнем, — сразу и бесплатно. */
    @Transactional
    public PlayerBastionResponse addSpecial(UUID bastionId, UUID memberId, ActivityRequests.AddSpecial request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        GameMembership membership = access.requireParticipant(bastion.getGameId(), userId);
        requireActive(bastion);
        PlayerBastionMember member = findMember(bastion, memberId);
        requireOwnerOrMaster(member, userId, membership.role() == GameRole.MASTER,
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
        PlayerBastionSetupRules.validateSpecial(member.getCharacterLevel(), special, reference);

        BastionFacility added = reference.get(request.facilityUrl());
        PlayerBastionFacility facility = new PlayerBastionFacility();
        facility.setMember(member);
        facility.setFacilityUrl(added.getUrl());
        facility.setSpace(added.getSpace());
        facility.setChoices(new ArrayList<>(Optional.ofNullable(request.choices()).orElse(List.of())));
        facility.setPrerequisiteConfirmed(added.getPrerequisite() == null);
        member.getFacilities().add(facility);

        return mapper.toResponse(saveWithNewVersion(bastion), membership, userId);
    }

    /**
     * Мастер убирает сооружение — так делается замена специализированного сооружения при
     * повышении уровня. Незавершённые приказы сооружения отменяются без возврата денег:
     * работа уже шла. Клетки сооружения исчезают с плана сами.
     */
    @Transactional
    public PlayerBastionResponse removeFacility(UUID bastionId, UUID facilityId) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        GameMembership membership = access.requireMaster(bastion.getGameId(), userId);
        PlayerBastionAccess.requireNotArchived(bastion);

        PlayerBastionFacility facility = findFacility(bastion, facilityId);
        orderRepository.findAllByBastionIdAndStatus(bastionId, OrderStatus.ACTIVE).stream()
                .filter(order -> facilityId.equals(order.getFacilityId()))
                .forEach(order -> {
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                });
        facility.getMember().getFacilities().remove(facility);
        return mapper.toResponse(saveWithNewVersion(bastion), membership, userId);
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
