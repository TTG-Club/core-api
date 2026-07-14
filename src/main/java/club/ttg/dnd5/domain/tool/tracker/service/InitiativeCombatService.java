package club.ttg.dnd5.domain.tool.tracker.service;

import club.ttg.dnd5.domain.tool.tracker.model.InitiativeParticipant;
import club.ttg.dnd5.domain.tool.tracker.model.InitiativeTracker;
import club.ttg.dnd5.domain.tool.tracker.model.TrackerStatus;
import club.ttg.dnd5.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Боевая логика трекера инициативы: броски d20, порядок хода и переключение ходов.
 * <p>
 * Порядок хода по правилам D&D: больший итог инициативы первее; при равном итоге первее
 * больший бонус инициативы; при полном равенстве — «монетка» ({@code tieRoll}, случайное
 * число, назначаемое при броске). «Монетка» хранится, поэтому порядок детерминирован:
 * участник, добавленный в идущий бой, не пересортировывает остальных.
 * <p>
 * Повержённые ({@code dead}) участники остаются в списке (и в сортировке), но пропускаются
 * при передаче хода.
 */
@Service
public class InitiativeCombatService {

    /**
     * Бросает d20 всем участникам (полный ре-ролл) и пересобирает порядок, но бой НЕ начинает —
     * старт выделен в отдельное действие ({@link #start}). Статус-агностично: в подготовке трекер
     * остаётся {@code PREPARING} и ход не назначается; в идущем бою это «ре-ролл» — раунд
     * возвращается к 1, ход переходит первому живому.
     */
    public void rollAll(InitiativeTracker tracker, List<InitiativeParticipant> participants) {
        if (participants.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Добавьте участников перед броском инициативы");
        }
        participants.forEach(this::roll);
        if (tracker.getStatus() == TrackerStatus.ACTIVE) {
            tracker.setRound(1);
            InitiativeParticipant first = firstAlive(participants);
            tracker.setCurrentParticipantId(first != null ? first.getId() : null);
        }
    }

    /**
     * Начинает бой, сохраняя уже введённые броски: участнику с готовым броском
     * ({@code initiativeRoll != null}) итог инициативы не меняется — переходит в бой как есть;
     * участнику без броска ставится {@code initiativeTotal = 0} (без доброса d20 и без учёта
     * бонуса; сам бонус в записи сохраняется на случай последующего броска). Порядок хода — по
     * итогу инициативы (не брошенные, 0, — в конце, в порядке добавления {@code seq}); инициативу
     * этим участникам мастер задаёт вручную или бросками по ходу боя. Ход — первому живому; все
     * повержены — текущего хода нет.
     */
    public void start(InitiativeTracker tracker, List<InitiativeParticipant> participants) {
        if (participants.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Добавьте участников перед началом боя");
        }
        participants.stream()
                .filter(participant -> participant.getInitiativeRoll() == null)
                .forEach(participant -> participant.setInitiativeTotal(0));
        startBattle(tracker, participants);
    }

    /** Бросает участнику d20 и назначает «монетку» тай-брейка. */
    public void roll(InitiativeParticipant participant) {
        participant.setInitiativeRoll(ThreadLocalRandom.current().nextInt(1, 21));
        participant.setTieRoll(ThreadLocalRandom.current().nextInt());
        recalculateTotal(participant);
    }

    /**
     * Пересчитывает итог инициативы (бросок + бонус) после правки броска или бонуса;
     * без броска итога нет. Броску, внесённому вручную, назначается недостающая «монетка».
     */
    public void recalculateTotal(InitiativeParticipant participant) {
        if (participant.getInitiativeRoll() == null) {
            participant.setInitiativeTotal(null);
            return;
        }
        if (participant.getTieRoll() == null) {
            participant.setTieRoll(ThreadLocalRandom.current().nextInt());
        }
        participant.setInitiativeTotal(participant.getInitiativeRoll() + participant.getInitiativeBonus());
    }

    /**
     * Передаёт ход следующему живому по порядку, пропуская повержённых; после последнего — новый
     * раунд и ход первому живому. Если у трекера включён {@code rerollEachRound}, на новом раунде
     * всем живым инициатива перебрасывается заново. Все повержены — текущего хода не остаётся.
     */
    public void nextTurn(InitiativeTracker tracker, List<InitiativeParticipant> participants) {
        if (tracker.getStatus() != TrackerStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Бой не начат — сначала прокиньте инициативу");
        }
        List<InitiativeParticipant> order = sort(participants);
        if (order.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "В трекере нет участников");
        }
        int currentIndex = indexOf(order, tracker.getCurrentParticipantId());
        TurnStep result = nextAlive(order, currentIndex, null);
        if (result.participant() == null) {
            tracker.setCurrentParticipantId(null);
            return;
        }
        InitiativeParticipant next = result.participant();
        if (result.wrapped()) {
            tracker.setRound(tracker.getRound() + 1);
            if (tracker.isRerollEachRound()) {
                order.stream().filter(participant -> !participant.isDead()).forEach(this::roll);
                next = firstAlive(participants);
            }
        }
        tracker.setCurrentParticipantId(next != null ? next.getId() : null);
    }

    /**
     * Откатывает ход на шаг назад: к предыдущему живому по порядку, пропуская повержённых;
     * с первого живого участника раунда — к последнему живому предыдущего раунда
     * ({@code round - 1}). На первом ходу первого раунда отката нет — состояние не меняется.
     * Живых нет или текущий ход не назначен — no-op. Броски инициативы не трогаются: при
     * включённом {@code rerollEachRound} значения прошлого раунда не хранятся и не
     * восстанавливаются.
     */
    public void prevTurn(InitiativeTracker tracker, List<InitiativeParticipant> participants) {
        if (tracker.getStatus() != TrackerStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Бой не начат — сначала прокиньте инициативу");
        }
        List<InitiativeParticipant> order = sort(participants);
        if (order.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "В трекере нет участников");
        }
        int currentIndex = indexOf(order, tracker.getCurrentParticipantId());
        if (currentIndex < 0) {
            return;
        }
        TurnStep result = prevAlive(order, currentIndex);
        if (result.participant() == null) {
            return;
        }
        if (result.wrapped()) {
            if (tracker.getRound() <= 1) {
                return;
            }
            tracker.setRound(tracker.getRound() - 1);
        }
        tracker.setCurrentParticipantId(result.participant().getId());
    }

    /**
     * Переносит ход с удаляемого участника на следующего живого (вызывается ДО удаления).
     * Удаляемый ходил последним в раунде — ход первому живому и новый раунд; живых больше
     * не остаётся — текущего хода нет.
     */
    public void onParticipantRemoval(InitiativeTracker tracker,
                                     List<InitiativeParticipant> participants,
                                     InitiativeParticipant removed) {
        if (!removed.getId().equals(tracker.getCurrentParticipantId())) {
            return;
        }
        List<InitiativeParticipant> order = sort(participants);
        int removedIndex = indexOf(order, removed.getId());
        TurnStep result = nextAlive(order, removedIndex, removed.getId());
        if (result.participant() == null) {
            tracker.setCurrentParticipantId(null);
            return;
        }
        if (result.wrapped()) {
            tracker.setRound(tracker.getRound() + 1);
        }
        tracker.setCurrentParticipantId(result.participant().getId());
    }

    /**
     * Возвращает трекер в подготовку: броски очищаются, повержённые «оживают», состав участников
     * сохраняется.
     */
    public void reset(InitiativeTracker tracker, List<InitiativeParticipant> participants) {
        tracker.setStatus(TrackerStatus.PREPARING);
        tracker.setRound(0);
        tracker.setCurrentParticipantId(null);
        participants.forEach(participant -> {
            participant.setInitiativeRoll(null);
            participant.setInitiativeTotal(null);
            participant.setTieRoll(null);
            participant.setDead(false);
        });
    }

    /** Участники в порядке хода; не бросавшие инициативу — в конце, в порядке добавления. */
    public static List<InitiativeParticipant> sort(Collection<InitiativeParticipant> participants) {
        return participants.stream()
                .sorted(InitiativeCombatService::compareTurnOrder)
                .toList();
    }

    private void startBattle(InitiativeTracker tracker, List<InitiativeParticipant> participants) {
        tracker.setStatus(TrackerStatus.ACTIVE);
        tracker.setRound(1);
        InitiativeParticipant first = firstAlive(participants);
        tracker.setCurrentParticipantId(first != null ? first.getId() : null);
    }

    private static InitiativeParticipant firstAlive(Collection<InitiativeParticipant> participants) {
        return sort(participants).stream()
                .filter(participant -> !participant.isDead())
                .findFirst()
                .orElse(null);
    }

    /**
     * Следующий живой участник после позиции {@code fromIndex} (по кругу), пропуская повержённых
     * и {@code excludedId} (удаляемого). {@code wrapped} — был ли переход через конец списка
     * (значит начался новый раунд). {@code participant == null} — живых не осталось.
     */
    private static TurnStep nextAlive(List<InitiativeParticipant> order, int fromIndex, UUID excludedId) {
        int size = order.size();
        boolean wrapped = false;
        for (int step = 1; step <= size; step++) {
            int raw = fromIndex + step;
            if (raw >= size && fromIndex >= 0) {
                wrapped = true;
            }
            InitiativeParticipant candidate = order.get(((raw % size) + size) % size);
            if (excludedId != null && excludedId.equals(candidate.getId())) {
                continue;
            }
            if (candidate.isDead()) {
                continue;
            }
            return new TurnStep(candidate, wrapped);
        }
        return new TurnStep(null, wrapped);
    }

    /**
     * Предыдущий живой участник перед позицией {@code fromIndex} (по кругу), пропуская
     * повержённых. {@code wrapped} — был ли переход через начало списка (значит откат в
     * предыдущий раунд). {@code participant == null} — живых не осталось.
     */
    private static TurnStep prevAlive(List<InitiativeParticipant> order, int fromIndex) {
        int size = order.size();
        boolean wrapped = false;
        for (int step = 1; step <= size; step++) {
            int raw = fromIndex - step;
            if (raw < 0) {
                wrapped = true;
            }
            InitiativeParticipant candidate = order.get(((raw % size) + size) % size);
            if (candidate.isDead()) {
                continue;
            }
            return new TurnStep(candidate, wrapped);
        }
        return new TurnStep(null, wrapped);
    }

    private static int compareTurnOrder(InitiativeParticipant left, InitiativeParticipant right) {
        if (left.getInitiativeTotal() == null || right.getInitiativeTotal() == null) {
            if (left.getInitiativeTotal() != null) {
                return -1;
            }
            if (right.getInitiativeTotal() != null) {
                return 1;
            }
            return compareBySeq(left, right);
        }
        int byTotal = right.getInitiativeTotal().compareTo(left.getInitiativeTotal());
        if (byTotal != 0) {
            return byTotal;
        }
        // При равном итоге брошенный участник идёт раньше не брошенного. Инициатива 0 без броска
        // (tieRoll == null, выставляется при /start) — это заглушка, а не результат броска. Единый
        // бинарный ключ «брошен ли» ОБЯЗАТЕЛЕН для транзитивности компаратора: без него брошенные с
        // итогом 0 (низкий бросок при отрицательном бонусе) сравнивались бы между собой по бонусу, а
        // с не брошенными — по seq, что образует цикл и нарушает контракт Comparator (IllegalArgumentException в sorted()).
        boolean leftRolled = left.getTieRoll() != null;
        boolean rightRolled = right.getTieRoll() != null;
        if (leftRolled != rightRolled) {
            return leftRolled ? -1 : 1;
        }
        // Тай-брейки D&D (бонус, затем «монетка») — только между брошенными; не брошенные с равным
        // итогом (0) сохраняют порядок добавления (seq).
        if (leftRolled) {
            int byBonus = Integer.compare(right.getInitiativeBonus(), left.getInitiativeBonus());
            if (byBonus != 0) {
                return byBonus;
            }
            int byTieRoll = Integer.compare(right.getTieRoll(), left.getTieRoll());
            if (byTieRoll != 0) {
                return byTieRoll;
            }
        }
        return compareBySeq(left, right);
    }

    /** По номеру добавления; при совпадении (теоретическая гонка добавлений) — по id, чтобы порядок был стабилен. */
    private static int compareBySeq(InitiativeParticipant left, InitiativeParticipant right) {
        int bySeq = Integer.compare(left.getSeq(), right.getSeq());
        if (bySeq != 0 || left.getId() == null || right.getId() == null) {
            return bySeq;
        }
        return left.getId().compareTo(right.getId());
    }

    private static int indexOf(List<InitiativeParticipant> order, UUID participantId) {
        if (participantId == null) {
            return -1;
        }
        for (int i = 0; i < order.size(); i++) {
            if (order.get(i).getId().equals(participantId)) {
                return i;
            }
        }
        return -1;
    }

    private record TurnStep(InitiativeParticipant participant, boolean wrapped) {
    }
}
