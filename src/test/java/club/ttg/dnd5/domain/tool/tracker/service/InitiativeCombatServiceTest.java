package club.ttg.dnd5.domain.tool.tracker.service;

import club.ttg.dnd5.domain.tool.tracker.model.InitiativeParticipant;
import club.ttg.dnd5.domain.tool.tracker.model.InitiativeTracker;
import club.ttg.dnd5.domain.tool.tracker.model.TrackerStatus;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitiativeCombatServiceTest {

    private final InitiativeCombatService service = new InitiativeCombatService();

    @Test
    void sortOrdersByTotalDescThenBonusDescThenTieRollDesc() {
        InitiativeParticipant lowTotal = participant(1, 10, 2, 100);
        InitiativeParticipant highTotal = participant(2, 18, 0, 100);
        InitiativeParticipant tiedHighBonus = participant(3, 15, 5, 100);
        InitiativeParticipant tiedLowBonus = participant(4, 15, 1, 100);
        InitiativeParticipant fullTieWins = participant(5, 15, 1, 999);

        List<InitiativeParticipant> order =
                InitiativeCombatService.sort(List.of(lowTotal, highTotal, tiedHighBonus, tiedLowBonus, fullTieWins));

        assertEquals(List.of(highTotal, tiedHighBonus, fullTieWins, tiedLowBonus, lowTotal), order);
    }

    @Test
    void sortKeepsComparatorContractWhenRolledAndUnrolledShareZeroTotal() {
        // Брошенные с итогом 0 (низкий бросок при отрицательном бонусе) и не брошенные (0 после
        // /start, tieRoll == null) при равном итоге не должны ломать контракт Comparator: брошенные
        // идут раньше (по бонусу ↓), не брошенные — в конце в порядке добавления. Иначе получался бы
        // цикл (rolledHigh < rolledLow по бонусу, но unrolled между ними по seq) и IllegalArgumentException в sorted().
        InitiativeParticipant unrolled = unrolled(2);
        unrolled.setInitiativeTotal(0);
        InitiativeParticipant rolledLowBonus = participant(1, 0, 1, 5);
        InitiativeParticipant rolledHighBonus = participant(3, 0, 100, 5);

        List<InitiativeParticipant> order =
                InitiativeCombatService.sort(List.of(unrolled, rolledLowBonus, rolledHighBonus));

        assertEquals(List.of(rolledHighBonus.getId(), rolledLowBonus.getId(), unrolled.getId()),
                order.stream().map(InitiativeParticipant::getId).toList());
    }

    @Test
    void sortPutsUnrolledParticipantsLastInSeqOrder() {
        InitiativeParticipant rolled = participant(5, 3, 0, 1);
        InitiativeParticipant unrolledFirst = unrolled(1);
        InitiativeParticipant unrolledSecond = unrolled(2);

        List<InitiativeParticipant> order =
                InitiativeCombatService.sort(List.of(unrolledSecond, rolled, unrolledFirst));

        assertEquals(List.of(rolled, unrolledFirst, unrolledSecond), order);
    }

    @Test
    void rollAllAssignsD20AndSortsWithoutStartingWhenPreparing() {
        InitiativeTracker tracker = tracker();
        InitiativeParticipant first = unrolled(1);
        first.setInitiativeBonus(3);
        InitiativeParticipant second = unrolled(2);
        second.setInitiativeBonus(-1);
        List<InitiativeParticipant> participants = List.of(first, second);

        service.rollAll(tracker, participants);

        for (InitiativeParticipant participant : participants) {
            assertNotNull(participant.getInitiativeRoll());
            assertTrue(participant.getInitiativeRoll() >= 1 && participant.getInitiativeRoll() <= 20);
            assertEquals(participant.getInitiativeRoll() + participant.getInitiativeBonus(),
                    participant.getInitiativeTotal());
            assertNotNull(participant.getTieRoll());
        }
        // Бросок не начинает бой: трекер остаётся в подготовке, ход не назначается
        assertEquals(TrackerStatus.PREPARING, tracker.getStatus());
        assertEquals(0, tracker.getRound());
        assertNull(tracker.getCurrentParticipantId());
    }

    @Test
    void rollAllWithoutParticipantsThrows() {
        assertThrows(ApiException.class, () -> service.rollAll(tracker(), List.of()));
    }

    @Test
    void nextTurnAdvancesToNextParticipant() {
        InitiativeParticipant first = participant(1, 20, 0, 1);
        InitiativeParticipant second = participant(2, 10, 0, 1);
        InitiativeTracker tracker = activeTracker(first.getId());

        service.nextTurn(tracker, List.of(first, second));

        assertEquals(second.getId(), tracker.getCurrentParticipantId());
        assertEquals(1, tracker.getRound());
    }

    @Test
    void nextTurnAfterLastParticipantStartsNewRound() {
        InitiativeParticipant first = participant(1, 20, 0, 1);
        InitiativeParticipant last = participant(2, 10, 0, 1);
        InitiativeTracker tracker = activeTracker(last.getId());

        service.nextTurn(tracker, List.of(first, last));

        assertEquals(first.getId(), tracker.getCurrentParticipantId());
        assertEquals(2, tracker.getRound());
    }

    @Test
    void nextTurnWithoutCurrentPointsToFirst() {
        InitiativeParticipant first = participant(1, 20, 0, 1);
        InitiativeParticipant second = participant(2, 10, 0, 1);
        InitiativeTracker tracker = activeTracker(null);

        service.nextTurn(tracker, List.of(second, first));

        assertEquals(first.getId(), tracker.getCurrentParticipantId());
        assertEquals(1, tracker.getRound());
    }

    @Test
    void nextTurnRequiresActiveCombat() {
        InitiativeTracker tracker = tracker();

        assertThrows(ApiException.class, () -> service.nextTurn(tracker, List.of(participant(1, 10, 0, 1))));
    }

    @Test
    void nextTurnWithoutParticipantsThrows() {
        assertThrows(ApiException.class, () -> service.nextTurn(activeTracker(null), List.of()));
    }

    @Test
    void prevTurnMovesToPreviousParticipant() {
        InitiativeParticipant first = participant(1, 20, 0, 1);
        InitiativeParticipant second = participant(2, 10, 0, 1);
        InitiativeTracker tracker = activeTracker(second.getId());

        service.prevTurn(tracker, List.of(first, second));

        assertEquals(first.getId(), tracker.getCurrentParticipantId());
        assertEquals(1, tracker.getRound());
    }

    @Test
    void prevTurnFromFirstParticipantReturnsToPreviousRound() {
        InitiativeParticipant first = participant(1, 20, 0, 1);
        InitiativeParticipant last = participant(2, 10, 0, 1);
        InitiativeTracker tracker = activeTracker(first.getId());
        tracker.setRound(3);

        service.prevTurn(tracker, List.of(first, last));

        assertEquals(last.getId(), tracker.getCurrentParticipantId());
        assertEquals(2, tracker.getRound());
    }

    @Test
    void prevTurnOnFirstTurnOfFirstRoundKeepsState() {
        InitiativeParticipant first = participant(1, 20, 0, 1);
        InitiativeParticipant last = participant(2, 10, 0, 1);
        InitiativeTracker tracker = activeTracker(first.getId());

        service.prevTurn(tracker, List.of(first, last));

        assertEquals(first.getId(), tracker.getCurrentParticipantId());
        assertEquals(1, tracker.getRound());
    }

    @Test
    void prevTurnSkipsDeadParticipants() {
        InitiativeParticipant first = participant(1, 20, 0, 1);
        InitiativeParticipant deadMiddle = participant(2, 15, 0, 1);
        deadMiddle.setDead(true);
        InitiativeParticipant last = participant(3, 10, 0, 1);
        InitiativeTracker tracker = activeTracker(last.getId());

        service.prevTurn(tracker, List.of(first, deadMiddle, last));

        // Мёртвый в середине порядка пропущен — откат сразу к первому
        assertEquals(first.getId(), tracker.getCurrentParticipantId());
        assertEquals(1, tracker.getRound());
    }

    @Test
    void prevTurnFromFirstAliveOfFirstRoundKeepsStateEvenWithDeadBefore() {
        // Текущий — первый ЖИВОЙ первого раунда (выше по порядку только повержённый):
        // отката нет, круг назад через конец списка означал бы раунд 0
        InitiativeParticipant deadFirst = participant(1, 20, 0, 1);
        deadFirst.setDead(true);
        InitiativeParticipant current = participant(2, 15, 0, 1);
        InitiativeParticipant last = participant(3, 10, 0, 1);
        InitiativeTracker tracker = activeTracker(current.getId());

        service.prevTurn(tracker, List.of(deadFirst, current, last));

        assertEquals(current.getId(), tracker.getCurrentParticipantId());
        assertEquals(1, tracker.getRound());
    }

    @Test
    void prevTurnWithSingleAliveDecrementsRoundKeepingTurn() {
        InitiativeParticipant only = participant(1, 20, 0, 1);
        InitiativeParticipant dead = participant(2, 10, 0, 1);
        dead.setDead(true);
        InitiativeTracker tracker = activeTracker(only.getId());
        tracker.setRound(2);

        service.prevTurn(tracker, List.of(only, dead));

        // Единственный живой: откат по кругу возвращается к нему же, но раундом раньше
        assertEquals(only.getId(), tracker.getCurrentParticipantId());
        assertEquals(1, tracker.getRound());
    }

    @Test
    void prevTurnWhenEveryoneDeadIsNoOp() {
        InitiativeParticipant a = participant(1, 20, 0, 1);
        a.setDead(true);
        InitiativeParticipant b = participant(2, 10, 0, 1);
        b.setDead(true);
        InitiativeTracker tracker = activeTracker(a.getId());
        tracker.setRound(2);

        service.prevTurn(tracker, List.of(a, b));

        assertEquals(a.getId(), tracker.getCurrentParticipantId());
        assertEquals(2, tracker.getRound());
    }

    @Test
    void prevTurnWithoutCurrentIsNoOp() {
        InitiativeTracker tracker = activeTracker(null);
        tracker.setRound(2);

        service.prevTurn(tracker, List.of(participant(1, 10, 0, 1)));

        assertNull(tracker.getCurrentParticipantId());
        assertEquals(2, tracker.getRound());
    }

    @Test
    void prevTurnRequiresActiveCombat() {
        assertThrows(ApiException.class, () -> service.prevTurn(tracker(), List.of(participant(1, 10, 0, 1))));
    }

    @Test
    void prevTurnWithoutParticipantsThrows() {
        assertThrows(ApiException.class, () -> service.prevTurn(activeTracker(null), List.of()));
    }

    @Test
    void removalOfCurrentParticipantPassesTurnToNext() {
        InitiativeParticipant current = participant(1, 20, 0, 1);
        InitiativeParticipant next = participant(2, 10, 0, 1);
        InitiativeTracker tracker = activeTracker(current.getId());

        service.onParticipantRemoval(tracker, List.of(current, next), current);

        assertEquals(next.getId(), tracker.getCurrentParticipantId());
        assertEquals(1, tracker.getRound());
    }

    @Test
    void removalOfLastCurrentParticipantWrapsToNewRound() {
        InitiativeParticipant first = participant(1, 20, 0, 1);
        InitiativeParticipant last = participant(2, 10, 0, 1);
        InitiativeTracker tracker = activeTracker(last.getId());

        service.onParticipantRemoval(tracker, List.of(first, last), last);

        assertEquals(first.getId(), tracker.getCurrentParticipantId());
        assertEquals(2, tracker.getRound());
    }

    @Test
    void removalOfOnlyParticipantClearsCurrentTurn() {
        InitiativeParticipant only = participant(1, 20, 0, 1);
        InitiativeTracker tracker = activeTracker(only.getId());

        service.onParticipantRemoval(tracker, List.of(only), only);

        assertNull(tracker.getCurrentParticipantId());
    }

    @Test
    void removalOfNonCurrentParticipantKeepsTurn() {
        InitiativeParticipant current = participant(1, 20, 0, 1);
        InitiativeParticipant other = participant(2, 10, 0, 1);
        InitiativeTracker tracker = activeTracker(current.getId());

        service.onParticipantRemoval(tracker, List.of(current, other), other);

        assertEquals(current.getId(), tracker.getCurrentParticipantId());
    }

    @Test
    void resetClearsRollsAndRevivesDeadReturningToPreparing() {
        InitiativeParticipant participant = participant(1, 15, 2, 42);
        participant.setDead(true);
        InitiativeTracker tracker = activeTracker(participant.getId());
        tracker.setRound(5);

        service.reset(tracker, List.of(participant));

        assertEquals(TrackerStatus.PREPARING, tracker.getStatus());
        assertEquals(0, tracker.getRound());
        assertNull(tracker.getCurrentParticipantId());
        assertNull(participant.getInitiativeRoll());
        assertNull(participant.getInitiativeTotal());
        assertNull(participant.getTieRoll());
        assertFalse(participant.isDead());
    }

    @Test
    void nextTurnSkipsDeadParticipants() {
        InitiativeParticipant first = participant(1, 20, 0, 1);
        InitiativeParticipant deadMiddle = participant(2, 15, 0, 1);
        deadMiddle.setDead(true);
        InitiativeParticipant last = participant(3, 10, 0, 1);
        InitiativeTracker tracker = activeTracker(first.getId());

        service.nextTurn(tracker, List.of(first, deadMiddle, last));

        // Мёртвый в середине порядка пропущен — ход сразу к третьему
        assertEquals(last.getId(), tracker.getCurrentParticipantId());
        assertEquals(1, tracker.getRound());
    }

    @Test
    void nextTurnClearsCurrentWhenEveryoneDead() {
        InitiativeParticipant a = participant(1, 20, 0, 1);
        a.setDead(true);
        InitiativeParticipant b = participant(2, 10, 0, 1);
        b.setDead(true);
        InitiativeTracker tracker = activeTracker(a.getId());

        service.nextTurn(tracker, List.of(a, b));

        assertNull(tracker.getCurrentParticipantId());
    }

    @Test
    void nextTurnRerollsAllAliveOnNewRoundWhenEnabled() {
        InitiativeParticipant first = participant(1, 20, 0, 1);
        InitiativeParticipant last = participant(2, 10, 0, 1);
        List<InitiativeParticipant> participants = List.of(first, last);
        InitiativeTracker tracker = activeTracker(last.getId());
        tracker.setRerollEachRound(true);

        service.nextTurn(tracker, participants);

        // Перешли на новый раунд → всем перекинули инициативу, ход первому в НОВОМ порядке
        assertEquals(2, tracker.getRound());
        UUID expectedFirst = InitiativeCombatService.sort(participants).getFirst().getId();
        assertEquals(expectedFirst, tracker.getCurrentParticipantId());
    }

    @Test
    void nextTurnKeepsRollsOnNewRoundWhenRerollDisabled() {
        InitiativeParticipant first = participant(1, 20, 0, 1);
        InitiativeParticipant last = participant(2, 10, 0, 1);
        InitiativeTracker tracker = activeTracker(last.getId());

        service.nextTurn(tracker, List.of(first, last));

        assertEquals(2, tracker.getRound());
        // Без ре-ролла итоги не тронуты
        assertEquals(20, first.getInitiativeTotal());
        assertEquals(10, last.getInitiativeTotal());
        assertEquals(first.getId(), tracker.getCurrentParticipantId());
    }

    @Test
    void startKeepsExistingRollsAndZeroesUnrolledInAdditionOrder() {
        InitiativeParticipant unrolledFirst = unrolled(1);
        unrolledFirst.setInitiativeBonus(5);
        InitiativeParticipant rolledHigh = participant(2, 18, 3, 100);
        InitiativeParticipant unrolledLast = unrolled(3);
        unrolledLast.setInitiativeBonus(-1);
        InitiativeParticipant rolledLow = participant(4, 7, 0, 50);
        InitiativeTracker tracker = tracker();
        List<InitiativeParticipant> participants = List.of(unrolledFirst, rolledHigh, unrolledLast, rolledLow);

        service.start(tracker, participants);

        // Брошенные — итог и бросок сохранены как есть (не перебрасываются, не обнуляются)
        assertEquals(18, rolledHigh.getInitiativeTotal());
        assertNotNull(rolledHigh.getInitiativeRoll());
        assertEquals(7, rolledLow.getInitiativeTotal());
        assertNotNull(rolledLow.getInitiativeRoll());
        // Не брошенные — инициатива 0, без доброса d20 и без бонуса
        assertEquals(0, unrolledFirst.getInitiativeTotal());
        assertNull(unrolledFirst.getInitiativeRoll());
        assertEquals(0, unrolledLast.getInitiativeTotal());
        assertNull(unrolledLast.getInitiativeRoll());
        // Бонус в записи сохранён (пригодится, если потом прокинуть)
        assertEquals(5, unrolledFirst.getInitiativeBonus());

        assertEquals(TrackerStatus.ACTIVE, tracker.getStatus());
        assertEquals(1, tracker.getRound());
        // Порядок: брошенные по итогу ↓, затем нулевые в конце в порядке добавления (seq)
        assertEquals(List.of(rolledHigh.getId(), rolledLow.getId(), unrolledFirst.getId(), unrolledLast.getId()),
                InitiativeCombatService.sort(participants).stream()
                        .map(InitiativeParticipant::getId).toList());
        // Ход — первому живому по порядку хода (брошенный с наибольшим итогом)
        assertEquals(rolledHigh.getId(), tracker.getCurrentParticipantId());
    }

    @Test
    void startSetsCurrentToFirstAliveByAdditionOrderSkippingDead() {
        InitiativeParticipant deadFirst = unrolled(1);
        deadFirst.setInitiativeBonus(10);
        deadFirst.setDead(true);
        InitiativeParticipant alive = unrolled(2);
        InitiativeTracker tracker = tracker();

        service.start(tracker, List.of(deadFirst, alive));

        // Мёртвый первый по порядку пропущен, несмотря на высокий бонус
        assertEquals(alive.getId(), tracker.getCurrentParticipantId());
        assertEquals(0, alive.getInitiativeTotal());
    }

    @Test
    void startClearsCurrentWhenEveryoneDead() {
        InitiativeParticipant a = unrolled(1);
        a.setDead(true);
        InitiativeParticipant b = unrolled(2);
        b.setDead(true);
        InitiativeTracker tracker = tracker();

        service.start(tracker, List.of(a, b));

        assertNull(tracker.getCurrentParticipantId());
        assertEquals(TrackerStatus.ACTIVE, tracker.getStatus());
        assertEquals(1, tracker.getRound());
    }

    @Test
    void startWithoutParticipantsThrows() {
        assertThrows(ApiException.class, () -> service.start(tracker(), List.of()));
    }

    @Test
    void rollAllInActiveCombatReRollsResettingRoundAndCurrentToFirstAlive() {
        InitiativeParticipant dead = unrolled(1);
        dead.setDead(true);
        InitiativeParticipant alive = unrolled(2);
        InitiativeTracker tracker = activeTracker(dead.getId());
        tracker.setRound(5);

        service.rollAll(tracker, List.of(dead, alive));

        // Ре-ролл в идущем бою: статус ACTIVE сохраняется, раунд с начала, ход первому живому
        assertEquals(TrackerStatus.ACTIVE, tracker.getStatus());
        assertEquals(1, tracker.getRound());
        assertEquals(alive.getId(), tracker.getCurrentParticipantId());
    }

    @Test
    void recalculateTotalAssignsMissingTieRollForManualRoll() {
        InitiativeParticipant participant = unrolled(1);
        participant.setInitiativeBonus(4);
        participant.setInitiativeRoll(17);

        service.recalculateTotal(participant);

        assertEquals(21, participant.getInitiativeTotal());
        assertNotNull(participant.getTieRoll());
    }

    @Test
    void recalculateTotalClearsTotalWithoutRoll() {
        InitiativeParticipant participant = unrolled(1);
        participant.setInitiativeTotal(15);

        service.recalculateTotal(participant);

        assertNull(participant.getInitiativeTotal());
    }

    private static InitiativeTracker tracker() {
        InitiativeTracker tracker = new InitiativeTracker();
        tracker.setId(UUID.randomUUID());
        tracker.setStatus(TrackerStatus.PREPARING);
        tracker.setRound(0);
        return tracker;
    }

    private static InitiativeTracker activeTracker(UUID currentParticipantId) {
        InitiativeTracker tracker = tracker();
        tracker.setStatus(TrackerStatus.ACTIVE);
        tracker.setRound(1);
        tracker.setCurrentParticipantId(currentParticipantId);
        return tracker;
    }

    private static InitiativeParticipant participant(int seq, int total, int bonus, int tieRoll) {
        InitiativeParticipant participant = unrolled(seq);
        participant.setInitiativeBonus(bonus);
        participant.setInitiativeRoll(Math.max(1, total - bonus));
        participant.setInitiativeTotal(total);
        participant.setTieRoll(tieRoll);
        return participant;
    }

    private static InitiativeParticipant unrolled(int seq) {
        InitiativeParticipant participant = new InitiativeParticipant();
        participant.setId(UUID.randomUUID());
        participant.setSeq(seq);
        return participant;
    }
}
