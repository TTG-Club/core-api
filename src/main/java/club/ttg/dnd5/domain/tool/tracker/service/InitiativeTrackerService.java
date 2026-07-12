package club.ttg.dnd5.domain.tool.tracker.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.beastiary.service.CreatureInitiativeCalculator;
import club.ttg.dnd5.domain.tool.tracker.model.InitiativeParticipant;
import club.ttg.dnd5.domain.tool.tracker.model.InitiativeTracker;
import club.ttg.dnd5.domain.tool.tracker.model.ParticipantType;
import club.ttg.dnd5.domain.tool.tracker.model.TrackerStatus;
import club.ttg.dnd5.domain.tool.tracker.repository.InitiativeParticipantRepository;
import club.ttg.dnd5.domain.tool.tracker.repository.InitiativeTrackerRepository;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.ParticipantAddRequest;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.ParticipantUpdateRequest;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.TrackerDetailedResponse;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.TrackerRequest;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.TrackerShortResponse;
import club.ttg.dnd5.domain.tool.tracker.rest.mapper.InitiativeTrackerMapper;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Трекер инициативы: создание и доступ, лимиты, состав участников.
 * Боевая логика (броски, порядок и переключение ходов) — в {@link InitiativeCombatService}.
 */
@RequiredArgsConstructor
@Service
public class InitiativeTrackerService {

    public static final String TRACKER_KEY_HEADER = "X-Tracker-Key";

    private static final int MAX_TRACKERS_PER_USER = 10;
    private static final int MAX_PLAYERS_PER_TRACKER = 50;
    private static final int MAX_CREATURES_PER_TRACKER = 100;
    private static final int MAX_DELETED_HISTORY_PER_USER = 30;
    private static final String DEFAULT_NAME = "Новый трекер";

    /** Хвостовой номер в имени существа («Гоблин 12» → 12) для продолжения нумерации. */
    private static final Pattern TRAILING_NUMBER = Pattern.compile("(\\d{1,6})$");

    private final InitiativeTrackerRepository trackerRepository;
    private final InitiativeParticipantRepository participantRepository;
    private final InitiativeCombatService combatService;
    private final TrackerCreationRateLimiter creationRateLimiter;
    private final CreatureRepository creatureRepository;
    private final InitiativeTrackerMapper trackerMapper;

    /**
     * Создаёт трекер. Авторизованному — до {@value MAX_TRACKERS_PER_USER} неудалённых, владение
     * по логину из JWT. Анониму — владение по секретному ключу {@code accessKey} (возвращается
     * в ответе, клиент хранит его в localStorage); создание ограничено по IP.
     */
    @Transactional
    public TrackerDetailedResponse create(TrackerRequest request, String clientIp) {
        User user = SecurityUtils.getUserOrNull();
        if (user == null) {
            creationRateLimiter.checkAnonymousCreation(clientIp);
        } else if (trackerRepository.countByOwnerUsernameAndDeletedFalse(user.getUsername()) >= MAX_TRACKERS_PER_USER) {
            throw new ApiException(HttpStatus.BAD_REQUEST, String.format(
                    "Достигнут лимит трекеров: %d. Удалите один из существующих", MAX_TRACKERS_PER_USER));
        }

        InitiativeTracker tracker = new InitiativeTracker();
        tracker.setName(nameOrDefault(request));
        tracker.setOwnerUsername(user != null ? user.getUsername() : null);
        tracker.setAccessKey(UUID.randomUUID());
        tracker.setStatus(TrackerStatus.PREPARING);
        tracker.setRound(0);
        tracker.setRerollEachRound(request != null && Boolean.TRUE.equals(request.getRerollEachRound()));

        // Флаш сразу: createdAt/updatedAt генерирует БД при INSERT, без него в ответе были бы null.
        InitiativeTracker saved = trackerRepository.saveAndFlush(tracker);
        return trackerMapper.toCreatedResponse(saved, List.of());
    }

    /**
     * Трекеры текущего пользователя, новые первее (createdAt — история создания).
     * {@code includeDeleted=true} — вместе с удалёнными (полная история).
     */
    public List<TrackerShortResponse> findMine(boolean includeDeleted) {
        String username = SecurityUtils.getUser().getUsername();
        List<InitiativeTracker> trackers = includeDeleted
                ? trackerRepository.findAllByOwnerUsernameOrderByCreatedAtDesc(username)
                : trackerRepository.findAllByOwnerUsernameAndDeletedFalseOrderByCreatedAtDesc(username);
        return trackerMapper.toShortResponseList(trackers);
    }

    public TrackerDetailedResponse findById(UUID trackerId, String trackerKey) {
        return toDetailedResponse(getWithAccess(trackerId, trackerKey));
    }

    /**
     * Обновление трекера: применяются только заполненные поля (имя, опция ре-ролла каждый раунд),
     * null — «не менять».
     */
    @Transactional
    public TrackerDetailedResponse updateSettings(UUID trackerId, TrackerRequest request, String trackerKey) {
        InitiativeTracker tracker = getWithAccess(trackerId, trackerKey);
        if (StringUtils.hasText(request.getName())) {
            tracker.setName(request.getName().trim());
        }
        if (request.getRerollEachRound() != null) {
            tracker.setRerollEachRound(request.getRerollEachRound());
        }
        return toDetailedResponse(tracker);
    }

    /**
     * Удаление: у трекера с владельцем — мягкое (строка остаётся в истории создания), анонимный
     * трекер удаляется физически вместе с участниками (истории у анонима нет).
     * <p>
     * При мягком удалении участники удаляются физически (они больше недоступны), а история
     * ограничивается последними {@value MAX_DELETED_HISTORY_PER_USER} удалёнными трекерами —
     * иначе цикл «создать → наполнить → удалить» рос бы в БД без ограничений: лимит
     * {@value MAX_TRACKERS_PER_USER} считает только неудалённые.
     */
    @Transactional
    public void delete(UUID trackerId, String trackerKey) {
        InitiativeTracker tracker = getWithAccess(trackerId, trackerKey);
        if (tracker.getOwnerUsername() == null) {
            trackerRepository.delete(tracker);
            return;
        }
        participantRepository.deleteAllByTrackerId(tracker.getId());
        tracker.setDeleted(true);
        trimDeletedHistory(tracker.getOwnerUsername());
    }

    private void trimDeletedHistory(String ownerUsername) {
        List<InitiativeTracker> deleted =
                trackerRepository.findAllByOwnerUsernameAndDeletedTrueOrderByUpdatedAtDesc(ownerUsername);
        if (deleted.size() > MAX_DELETED_HISTORY_PER_USER) {
            trackerRepository.deleteAll(deleted.subList(MAX_DELETED_HISTORY_PER_USER, deleted.size()));
        }
    }

    /**
     * Добавляет игрока (по одному) или существ из бестиария (пачкой). Участник, добавленный
     * в идущий бой, сразу получает бросок инициативы и встаёт в порядок хода.
     */
    @Transactional
    public TrackerDetailedResponse addParticipants(UUID trackerId, ParticipantAddRequest request, String trackerKey) {
        InitiativeTracker tracker = getWithAccess(trackerId, trackerKey);
        List<InitiativeParticipant> added = request.getType() == ParticipantType.PLAYER
                ? List.of(buildPlayer(tracker, request))
                : buildCreatures(tracker, request);
        if (tracker.getStatus() == TrackerStatus.ACTIVE) {
            added.forEach(combatService::roll);
        }
        participantRepository.saveAll(added);
        touch(tracker);
        return toDetailedResponse(tracker);
    }

    /** Правка участника: применяются только заполненные поля запроса. */
    @Transactional
    public TrackerDetailedResponse updateParticipant(UUID trackerId,
                                                     UUID participantId,
                                                     ParticipantUpdateRequest request,
                                                     String trackerKey) {
        InitiativeTracker tracker = getWithAccess(trackerId, trackerKey);
        InitiativeParticipant participant = getParticipant(trackerId, participantId);
        if (StringUtils.hasText(request.getName())) {
            participant.setName(request.getName().trim());
        }
        if (request.getInitiativeBonus() != null) {
            participant.setInitiativeBonus(request.getInitiativeBonus());
        }
        if (request.getInitiativeRoll() != null) {
            participant.setInitiativeRoll(request.getInitiativeRoll());
        }
        if (request.getDead() != null) {
            participant.setDead(request.getDead());
        }
        combatService.recalculateTotal(participant);
        touch(tracker);
        return toDetailedResponse(tracker);
    }

    /** Бросает d20 одному участнику (по одному, не начиная бой и не трогая остальных). */
    @Transactional
    public TrackerDetailedResponse rollParticipant(UUID trackerId, UUID participantId, String trackerKey) {
        InitiativeTracker tracker = getWithAccess(trackerId, trackerKey);
        InitiativeParticipant participant = getParticipant(trackerId, participantId);
        combatService.roll(participant);
        touch(tracker);
        return toDetailedResponse(tracker);
    }

    @Transactional
    public TrackerDetailedResponse deleteParticipant(UUID trackerId, UUID participantId, String trackerKey) {
        InitiativeTracker tracker = getWithAccess(trackerId, trackerKey);
        InitiativeParticipant participant = getParticipant(trackerId, participantId);
        combatService.onParticipantRemoval(tracker, participantRepository.findAllByTrackerId(trackerId), participant);
        participantRepository.delete(participant);
        touch(tracker);
        return toDetailedResponse(tracker);
    }

    /**
     * Бросает d20 всем участникам и пересобирает порядок, но бой не начинает (для старта —
     * {@link #start}). В подготовке статус остаётся {@code PREPARING}; в идущем бою — ре-ролл
     * (раунд с начала, ход первому живому).
     */
    @Transactional
    public TrackerDetailedResponse rollInitiative(UUID trackerId, String trackerKey) {
        InitiativeTracker tracker = getWithAccess(trackerId, trackerKey);
        combatService.rollAll(tracker, participantRepository.findAllByTrackerId(trackerId));
        return toDetailedResponse(tracker);
    }

    /**
     * Начинает бой, сохраняя уже введённые броски: у кого инициатива уже задана/брошена — итог
     * переходит в бой как есть; остальным {@code initiativeTotal = 0} (без доброса d20 и без
     * бонуса). Инициативу нулевым участникам мастер задаёт вручную (правкой участника) или
     * бросками по ходу боя. В отличие от {@link #rollInitiative} (случайный d20 + бонус всем и
     * полная сортировка).
     */
    @Transactional
    public TrackerDetailedResponse start(UUID trackerId, String trackerKey) {
        InitiativeTracker tracker = getWithAccess(trackerId, trackerKey);
        combatService.start(tracker, participantRepository.findAllByTrackerId(trackerId));
        return toDetailedResponse(tracker);
    }

    @Transactional
    public TrackerDetailedResponse nextTurn(UUID trackerId, String trackerKey) {
        InitiativeTracker tracker = getWithAccess(trackerId, trackerKey);
        combatService.nextTurn(tracker, participantRepository.findAllByTrackerId(trackerId));
        return toDetailedResponse(tracker);
    }

    /** Завершает бой: броски очищаются, состав сохраняется, трекер снова в подготовке. */
    @Transactional
    public TrackerDetailedResponse reset(UUID trackerId, String trackerKey) {
        InitiativeTracker tracker = getWithAccess(trackerId, trackerKey);
        combatService.reset(tracker, participantRepository.findAllByTrackerId(trackerId));
        return toDetailedResponse(tracker);
    }

    private InitiativeParticipant buildPlayer(InitiativeTracker tracker, ParticipantAddRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Укажите имя игрока");
        }
        validatePlayerLimit(tracker.getId(), 1);
        InitiativeParticipant participant =
                newParticipant(tracker, ParticipantType.PLAYER, participantRepository.findMaxSeq(tracker.getId()) + 1);
        participant.setName(request.getName().trim());
        participant.setInitiativeBonus(request.getInitiativeBonus() != null ? request.getInitiativeBonus() : 0);
        return participant;
    }

    private List<InitiativeParticipant> buildCreatures(InitiativeTracker tracker, ParticipantAddRequest request) {
        if (!StringUtils.hasText(request.getCreatureUrl())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Укажите существо из бестиария (creatureUrl)");
        }
        int count = request.getCount() != null ? request.getCount() : 1;
        validateCreatureLimit(tracker.getId(), count);
        Creature creature = creatureRepository.findById(request.getCreatureUrl())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Существо с URL: %s не существует", request.getCreatureUrl())));

        // Имя и бонус — снапшот из бестиария на момент добавления: правка бестиария не влияет на бой.
        int bonus = CreatureInitiativeCalculator.initiativeBonus(creature);
        String baseName = StringUtils.hasText(request.getName()) ? request.getName().trim() : creature.getName();
        List<InitiativeParticipant> current = participantRepository.findAllByTrackerId(tracker.getId());
        int nextNumber = nextCreatureNumber(current, creature.getUrl());
        int maxSeq = current.stream().mapToInt(InitiativeParticipant::getSeq).max().orElse(0);

        List<InitiativeParticipant> creatures = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            InitiativeParticipant participant = newParticipant(tracker, ParticipantType.CREATURE, maxSeq + i + 1);
            participant.setName(String.format("%s %d", baseName, nextNumber + i));
            participant.setInitiativeBonus(bonus);
            participant.setCreatureUrl(creature.getUrl());
            creatures.add(participant);
        }
        return creatures;
    }

    /**
     * Следующий номер для имени существа: максимальный хвостовой номер среди участников этого же
     * существа + 1. Нумерация по максимуму, а не по количеству — после удаления «Гоблина 2»
     * следующий станет «Гоблином 4», а не дублем «Гоблина 3».
     */
    private static int nextCreatureNumber(List<InitiativeParticipant> participants, String creatureUrl) {
        int max = 0;
        for (InitiativeParticipant participant : participants) {
            if (!creatureUrl.equals(participant.getCreatureUrl())) {
                continue;
            }
            Matcher matcher = TRAILING_NUMBER.matcher(participant.getName());
            if (matcher.find()) {
                max = Math.max(max, Integer.parseInt(matcher.group(1)));
            }
        }
        return max + 1;
    }

    private InitiativeParticipant newParticipant(InitiativeTracker tracker, ParticipantType type, int seq) {
        InitiativeParticipant participant = new InitiativeParticipant();
        participant.setTrackerId(tracker.getId());
        participant.setType(type);
        participant.setSeq(seq);
        return participant;
    }

    private void validatePlayerLimit(UUID trackerId, int toAdd) {
        if (participantRepository.countByTrackerIdAndType(trackerId, ParticipantType.PLAYER) + toAdd
                > MAX_PLAYERS_PER_TRACKER) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    String.format("Достигнут лимит игроков в трекере: %d", MAX_PLAYERS_PER_TRACKER));
        }
    }

    private void validateCreatureLimit(UUID trackerId, int toAdd) {
        if (participantRepository.countByTrackerIdAndType(trackerId, ParticipantType.CREATURE) + toAdd
                > MAX_CREATURES_PER_TRACKER) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    String.format("Достигнут лимит существ в трекере: %d", MAX_CREATURES_PER_TRACKER));
        }
    }

    private InitiativeTracker getWithAccess(UUID trackerId, String trackerKey) {
        InitiativeTracker tracker = trackerRepository.findById(trackerId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Трекер с id %s не существует", trackerId)));
        requireAccess(tracker, trackerKey);
        return tracker;
    }

    /**
     * Трекер с владельцем доступен только владельцу (по логину из JWT), анонимный — по
     * секретному ключу из заголовка {@value TRACKER_KEY_HEADER}.
     */
    private void requireAccess(InitiativeTracker tracker, String trackerKey) {
        if (tracker.getOwnerUsername() != null) {
            User user = SecurityUtils.getUserOrNull();
            if (user == null || !tracker.getOwnerUsername().equals(user.getUsername())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Доступ к трекеру запрещен");
            }
            return;
        }
        if (trackerKey == null || !trackerKey.trim().equalsIgnoreCase(tracker.getAccessKey().toString())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Доступ к трекеру запрещен");
        }
    }

    private InitiativeParticipant getParticipant(UUID trackerId, UUID participantId) {
        return participantRepository.findByIdAndTrackerId(participantId, trackerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Участник с id %s не существует", participantId)));
    }

    private TrackerDetailedResponse toDetailedResponse(InitiativeTracker tracker) {
        List<InitiativeParticipant> order =
                InitiativeCombatService.sort(participantRepository.findAllByTrackerId(tracker.getId()));
        return trackerMapper.toDetailedResponse(tracker, order);
    }

    /**
     * Отметка активности для TTL-очистки анонимных трекеров: операции только с участниками
     * не меняют строку трекера, поэтому updated_at обновляется явно. Значение дублируется
     * в загруженную сущность — bulk-update не виден persistence context, а ответ должен
     * отдавать свежий updatedAt.
     */
    private void touch(InitiativeTracker tracker) {
        Instant now = Instant.now();
        trackerRepository.touch(tracker.getId(), now);
        tracker.setUpdatedAt(now);
    }

    private String nameOrDefault(TrackerRequest request) {
        return request != null && StringUtils.hasText(request.getName())
                ? request.getName().trim()
                : DEFAULT_NAME;
    }
}
