package club.ttg.dnd5.domain.initiative.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.beastiary.rest.mapper.CreatureMapper;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.ChallengeRating;
import club.ttg.dnd5.domain.initiative.model.EncounterDifficulty;
import club.ttg.dnd5.domain.initiative.model.EncounterDifficultyLevel;
import club.ttg.dnd5.domain.initiative.model.InitiativeParticipant;
import club.ttg.dnd5.domain.initiative.model.InitiativeParticipantState;
import club.ttg.dnd5.domain.initiative.model.InitiativeParticipantType;
import club.ttg.dnd5.domain.initiative.model.InitiativeRelationType;
import club.ttg.dnd5.domain.initiative.model.InitiativeRollMode;
import club.ttg.dnd5.domain.initiative.model.InitiativeTracker;
import club.ttg.dnd5.domain.initiative.model.InitiativeTrackerStatus;
import club.ttg.dnd5.domain.initiative.model.TemporaryHpMode;
import club.ttg.dnd5.domain.initiative.repository.InitiativeParticipantRepository;
import club.ttg.dnd5.domain.initiative.repository.InitiativeTrackerRepository;
import club.ttg.dnd5.domain.initiative.rest.dto.ActiveParticipantResponse;
import club.ttg.dnd5.domain.initiative.rest.dto.HpAmountRequest;
import club.ttg.dnd5.domain.initiative.rest.dto.HpUpdateRequest;
import club.ttg.dnd5.domain.initiative.rest.dto.InitiativeParticipantRequest;
import club.ttg.dnd5.domain.initiative.rest.dto.InitiativeParticipantResponse;
import club.ttg.dnd5.domain.initiative.rest.dto.InitiativeTrackerCreateRequest;
import club.ttg.dnd5.domain.initiative.rest.dto.InitiativeTrackerResponse;
import club.ttg.dnd5.domain.initiative.rest.dto.InitiativeTrackerUpdateRequest;
import club.ttg.dnd5.domain.initiative.rest.dto.ParticipantStateRequest;
import club.ttg.dnd5.domain.initiative.rest.dto.RollInitiativeRequest;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InitiativeTrackerService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final InitiativeTrackerRepository trackerRepository;
    private final InitiativeParticipantRepository participantRepository;
    private final CreatureRepository creatureRepository;
    private final CreatureMapper creatureMapper;

    @Transactional
    public InitiativeTrackerResponse current() {
        var ownerId = currentOwnerId();
        var tracker = trackerRepository.findFirstByOwnerIdAndStatusNotOrderByUpdatedAtDesc(ownerId, InitiativeTrackerStatus.FINISHED)
                .orElseGet(() -> createTracker(ownerId, new InitiativeTrackerCreateRequest()));
        return toResponse(tracker);
    }

    @Transactional
    public InitiativeTrackerResponse create(InitiativeTrackerCreateRequest request) {
        return toResponse(createTracker(currentOwnerId(), request));
    }

    @Transactional(readOnly = true)
    public InitiativeTrackerResponse find(UUID trackerId) {
        return toResponse(findOwnedTracker(trackerId));
    }

    @Transactional
    public InitiativeTrackerResponse update(UUID trackerId, InitiativeTrackerUpdateRequest request) {
        var tracker = findOwnedTracker(trackerId);
        if (StringUtils.hasText(request.getTitle())) {
            tracker.setTitle(request.getTitle());
        }
        if (request.getRerollEachRound() != null) {
            tracker.setRerollEachRound(request.getRerollEachRound());
        }
        if (request.getGroupSameCreaturesInitiative() != null) {
            tracker.setGroupSameCreaturesInitiative(request.getGroupSameCreaturesInitiative());
        }
        return toResponse(trackerRepository.save(tracker));
    }

    @Transactional
    public void delete(UUID trackerId) {
        trackerRepository.delete(findOwnedTracker(trackerId));
    }

    @Transactional
    public InitiativeTrackerResponse addParticipants(UUID trackerId, InitiativeParticipantRequest request) {
        var tracker = findOwnedTracker(trackerId);
        var count = Math.max(1, request.getCount() == null ? 1 : request.getCount());
        for (int i = 0; i < count; i++) {
            var participant = request.getType() == InitiativeParticipantType.CREATURE || StringUtils.hasText(request.getSourceCreatureId())
                    ? buildCreatureParticipant(tracker, request, i)
                    : buildPlayerParticipant(tracker, request);
            participantRepository.save(participant);
        }
        if (tracker.getStatus() == InitiativeTrackerStatus.ACTIVE) {
            rollMissingInitiatives(tracker);
            sortParticipants(tracker);
            tracker.setEncounterDifficulty(calculateDifficulty(tracker));
        }
        return toResponse(trackerRepository.save(tracker));
    }

    @Transactional
    public InitiativeTrackerResponse updateParticipant(UUID trackerId, UUID participantId, InitiativeParticipantRequest request) {
        var tracker = findOwnedTracker(trackerId);
        var participant = findParticipant(tracker, participantId);
        if (StringUtils.hasText(request.getName())) {
            participant.setName(request.getName());
            participant.setDisplayName(request.getName());
        }
        if (request.getRelationType() != null) {
            participant.setRelationType(request.getRelationType());
        }
        if (request.getLevel() != null) {
            participant.setLevel(request.getLevel());
        }
        if (request.getHpMax() != null) {
            participant.setHpMax(Math.max(0, request.getHpMax()));
        }
        if (request.getHpCurrent() != null) {
            participant.setHpCurrent(request.getHpCurrent());
        }
        if (request.getHpTemporary() != null) {
            participant.setHpTemporary(Math.max(0, request.getHpTemporary()));
        }
        if (request.getInitiativeBonus() != null) {
            participant.setInitiativeBonus(request.getInitiativeBonus());
        }
        if (request.getDexterityBonus() != null) {
            participant.setDexterityBonus(request.getDexterityBonus());
        }
        if (request.getRollMode() != null) {
            participant.setRollMode(request.getRollMode());
        }
        if (request.getRollValue() != null) {
            participant.setRollValue(request.getRollValue());
            participant.setInitiativeTotal(request.getRollValue() + participant.getInitiativeBonus());
        }
        updateStateFromHp(participant);
        participantRepository.save(participant);
        sortParticipants(tracker);
        return toResponse(tracker);
    }

    @Transactional
    public InitiativeTrackerResponse removeParticipant(UUID trackerId, UUID participantId) {
        var tracker = findOwnedTracker(trackerId);
        participantRepository.deleteByIdAndTracker(participantId, tracker);
        var participants = participants(tracker);
        if (participantId.equals(tracker.getCurrentParticipantId())) {
            tracker.setCurrentParticipantId(firstAlive(participants).map(InitiativeParticipant::getId).orElse(null));
        }
        tracker.setEncounterDifficulty(calculateDifficulty(tracker));
        return toResponse(trackerRepository.save(tracker));
    }

    @Transactional
    public InitiativeTrackerResponse rollInitiative(UUID trackerId, UUID participantId, RollInitiativeRequest request) {
        var tracker = findOwnedTracker(trackerId);
        var participant = findParticipant(tracker, participantId);
        if (request.getRollMode() != null) {
            participant.setRollMode(request.getRollMode());
        }
        if (participant.getRollMode() == InitiativeRollMode.MANUAL && request.getRollValue() != null) {
            applyManualRoll(participant, request.getRollValue());
        } else {
            rollInitiative(participant);
        }
        participantRepository.save(participant);
        sortParticipants(tracker);
        return toResponse(tracker);
    }

    @Transactional
    public InitiativeTrackerResponse bulkRollInitiative(UUID trackerId) {
        var tracker = findOwnedTracker(trackerId);
        participants(tracker).forEach(this::rollInitiative);
        sortParticipants(tracker);
        return toResponse(tracker);
    }

    @Transactional
    public InitiativeTrackerResponse start(UUID trackerId) {
        var tracker = findOwnedTracker(trackerId);
        var participants = participants(tracker);
        if (participants.isEmpty()) {
            throw new IllegalStateException("Initiative tracker has no participants");
        }
        rollMissingInitiatives(tracker);
        sortParticipants(tracker);
        tracker.setStatus(InitiativeTrackerStatus.ACTIVE);
        tracker.setCurrentRound(1);
        tracker.setCurrentParticipantId(firstAlive(participants(tracker))
                .map(InitiativeParticipant::getId)
                .orElse(participants(tracker).getFirst().getId()));
        tracker.setEncounterDifficulty(calculateDifficulty(tracker));
        return toResponse(trackerRepository.save(tracker));
    }

    @Transactional
    public InitiativeTrackerResponse finish(UUID trackerId) {
        var tracker = findOwnedTracker(trackerId);
        tracker.setStatus(InitiativeTrackerStatus.FINISHED);
        return toResponse(trackerRepository.save(tracker));
    }

    @Transactional
    public InitiativeTrackerResponse nextTurn(UUID trackerId) {
        var tracker = findOwnedTracker(trackerId);
        moveTurn(tracker, 1);
        return toResponse(trackerRepository.save(tracker));
    }

    @Transactional
    public InitiativeTrackerResponse previousTurn(UUID trackerId) {
        var tracker = findOwnedTracker(trackerId);
        moveTurn(tracker, -1);
        return toResponse(trackerRepository.save(tracker));
    }

    @Transactional
    public InitiativeTrackerResponse nextRound(UUID trackerId) {
        var tracker = findOwnedTracker(trackerId);
        tracker.setCurrentRound(tracker.getCurrentRound() + 1);
        if (tracker.isRerollEachRound()) {
            rerollRound(tracker);
        }
        tracker.setCurrentParticipantId(firstAlive(participants(tracker)).map(InitiativeParticipant::getId).orElse(null));
        return toResponse(trackerRepository.save(tracker));
    }

    @Transactional
    public InitiativeTrackerResponse previousRound(UUID trackerId) {
        var tracker = findOwnedTracker(trackerId);
        tracker.setCurrentRound(Math.max(1, tracker.getCurrentRound() - 1));
        tracker.setCurrentParticipantId(firstAlive(participants(tracker)).map(InitiativeParticipant::getId).orElse(null));
        return toResponse(trackerRepository.save(tracker));
    }

    @Transactional
    public InitiativeTrackerResponse rerollRound(UUID trackerId) {
        var tracker = findOwnedTracker(trackerId);
        rerollRound(tracker);
        return toResponse(tracker);
    }

    @Transactional
    public InitiativeTrackerResponse damage(UUID trackerId, UUID participantId, HpAmountRequest request) {
        var tracker = findOwnedTracker(trackerId);
        var participant = findParticipant(tracker, participantId);
        var amount = Math.max(0, request.getAmount());
        var absorbed = Math.min(participant.getHpTemporary(), amount);
        participant.setHpTemporary(participant.getHpTemporary() - absorbed);
        participant.setHpCurrent(participant.getHpCurrent() - (amount - absorbed));
        updateStateFromHp(participant);
        participantRepository.save(participant);
        return toResponse(tracker);
    }

    @Transactional
    public InitiativeTrackerResponse heal(UUID trackerId, UUID participantId, HpAmountRequest request) {
        var tracker = findOwnedTracker(trackerId);
        var participant = findParticipant(tracker, participantId);
        participant.setHpCurrent(Math.min(participant.getHpMax(), participant.getHpCurrent() + Math.max(0, request.getAmount())));
        updateStateFromHp(participant);
        participantRepository.save(participant);
        return toResponse(tracker);
    }

    @Transactional
    public InitiativeTrackerResponse temporaryHp(UUID trackerId, UUID participantId, HpAmountRequest request) {
        var tracker = findOwnedTracker(trackerId);
        var participant = findParticipant(tracker, participantId);
        var mode = request.getMode() == null ? TemporaryHpMode.MAX : request.getMode();
        var amount = Math.max(0, request.getAmount());
        switch (mode) {
            case CLEAR -> participant.setHpTemporary(0);
            case REPLACE -> participant.setHpTemporary(amount);
            case MAX -> participant.setHpTemporary(Math.max(participant.getHpTemporary(), amount));
        }
        participantRepository.save(participant);
        return toResponse(tracker);
    }

    @Transactional
    public InitiativeTrackerResponse updateHp(UUID trackerId, UUID participantId, HpUpdateRequest request) {
        var tracker = findOwnedTracker(trackerId);
        var participant = findParticipant(tracker, participantId);
        if (request.getHpMax() != null) {
            participant.setHpMax(Math.max(0, request.getHpMax()));
        }
        if (request.getHpCurrent() != null) {
            participant.setHpCurrent(request.getHpCurrent());
        }
        if (request.getHpTemporary() != null) {
            participant.setHpTemporary(Math.max(0, request.getHpTemporary()));
        }
        updateStateFromHp(participant);
        participantRepository.save(participant);
        return toResponse(tracker);
    }

    @Transactional
    public InitiativeTrackerResponse updateState(UUID trackerId, UUID participantId, ParticipantStateRequest request) {
        var tracker = findOwnedTracker(trackerId);
        var participant = findParticipant(tracker, participantId);
        participant.setState(request.getState() == null ? InitiativeParticipantState.ACTIVE : request.getState());
        participantRepository.save(participant);
        return toResponse(tracker);
    }

    @Transactional(readOnly = true)
    public EncounterDifficulty difficulty(UUID trackerId) {
        return findOwnedTracker(trackerId).getEncounterDifficulty();
    }

    @Transactional
    public InitiativeTrackerResponse recalculateDifficulty(UUID trackerId) {
        var tracker = findOwnedTracker(trackerId);
        tracker.setEncounterDifficulty(calculateDifficulty(tracker));
        return toResponse(trackerRepository.save(tracker));
    }

    @Transactional(readOnly = true)
    public ActiveParticipantResponse active(UUID trackerId) {
        var tracker = findOwnedTracker(trackerId);
        return activeResponse(tracker);
    }

    @Transactional(readOnly = true)
    public ActiveParticipantResponse sharedActive(String shareToken) {
        var tracker = trackerRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new EntityNotFoundException("Shared initiative tracker not found"));
        return activeResponse(tracker);
    }

    @Transactional
    public InitiativeTrackerResponse share(UUID trackerId) {
        var tracker = findOwnedTracker(trackerId);
        if (!StringUtils.hasText(tracker.getShareToken())) {
            tracker.setShareToken(UUID.randomUUID().toString());
        }
        return toResponse(trackerRepository.save(tracker));
    }

    @Transactional
    public InitiativeTrackerResponse unshare(UUID trackerId) {
        var tracker = findOwnedTracker(trackerId);
        tracker.setShareToken(null);
        return toResponse(trackerRepository.save(tracker));
    }

    @Transactional(readOnly = true)
    public InitiativeTrackerResponse shared(String shareToken) {
        return toResponse(trackerRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new EntityNotFoundException("Shared initiative tracker not found")));
    }

    private InitiativeTracker createTracker(UUID ownerId, InitiativeTrackerCreateRequest request) {
        var tracker = new InitiativeTracker();
        tracker.setOwnerId(ownerId);
        if (StringUtils.hasText(request.getTitle())) {
            tracker.setTitle(request.getTitle());
        }
        tracker.setRerollEachRound(request.isRerollEachRound());
        tracker.setGroupSameCreaturesInitiative(request.isGroupSameCreaturesInitiative());
        return trackerRepository.save(tracker);
    }

    private InitiativeParticipant buildPlayerParticipant(InitiativeTracker tracker, InitiativeParticipantRequest request) {
        var participant = new InitiativeParticipant();
        participant.setTracker(tracker);
        participant.setType(InitiativeParticipantType.PLAYER);
        participant.setRelationType(request.getRelationType() == null ? InitiativeRelationType.ALLY : request.getRelationType());
        participant.setName(StringUtils.hasText(request.getName()) ? request.getName() : "Player");
        participant.setBaseName(participant.getName());
        participant.setDisplayName(participant.getName());
        participant.setLevel(request.getLevel());
        participant.setHpMax(request.getHpMax() == null ? 0 : Math.max(0, request.getHpMax()));
        participant.setHpCurrent(request.getHpCurrent() == null ? participant.getHpMax() : request.getHpCurrent());
        participant.setHpTemporary(request.getHpTemporary() == null ? 0 : Math.max(0, request.getHpTemporary()));
        participant.setInitiativeBonus(request.getInitiativeBonus() == null ? 0 : request.getInitiativeBonus());
        participant.setDexterityBonus(request.getDexterityBonus() == null ? participant.getInitiativeBonus() : request.getDexterityBonus());
        participant.setRollMode(request.getRollMode() == null ? InitiativeRollMode.MANUAL : request.getRollMode());
        participant.setOrderIndex(nextOrderIndex(tracker));
        participant.setAddedRound(tracker.getCurrentRound());
        if (request.getRollValue() != null) {
            applyManualRoll(participant, request.getRollValue());
        }
        updateStateFromHp(participant);
        return participant;
    }

    private InitiativeParticipant buildCreatureParticipant(InitiativeTracker tracker, InitiativeParticipantRequest request, int offset) {
        var creature = creatureRepository.findById(request.getSourceCreatureId())
                .orElseThrow(() -> new EntityNotFoundException("Creature not found: " + request.getSourceCreatureId()));
        var participant = new InitiativeParticipant();
        participant.setTracker(tracker);
        participant.setType(InitiativeParticipantType.CREATURE);
        participant.setRelationType(request.getRelationType() == null ? InitiativeRelationType.ENEMY : request.getRelationType());
        participant.setSourceCreature(creature);
        participant.setBaseName(creature.getName());
        var sameCreatureIndex = participantRepository.countByTrackerAndSourceCreatureUrl(tracker, creature.getUrl()) + offset + 1;
        participant.setSameCreatureIndex(sameCreatureIndex);
        participant.setName("%s %d".formatted(creature.getName(), sameCreatureIndex));
        participant.setDisplayName(participant.getName());
        var maxHp = request.getHpMax() == null ? creatureHp(creature) : Math.max(0, request.getHpMax());
        participant.setHpMax(maxHp);
        participant.setHpCurrent(request.getHpCurrent() == null ? maxHp : request.getHpCurrent());
        participant.setHpTemporary(request.getHpTemporary() == null ? 0 : Math.max(0, request.getHpTemporary()));
        participant.setInitiativeBonus(request.getInitiativeBonus() == null ? creatureInitiativeBonus(creature) : request.getInitiativeBonus());
        participant.setDexterityBonus(request.getDexterityBonus() == null ? creatureDexterityBonus(creature) : request.getDexterityBonus());
        participant.setRollMode(request.getRollMode() == null ? InitiativeRollMode.NORMAL : request.getRollMode());
        participant.setOrderIndex(nextOrderIndex(tracker) + offset);
        participant.setAddedRound(tracker.getCurrentRound());
        if (request.getRollValue() != null) {
            applyManualRoll(participant, request.getRollValue());
        }
        updateStateFromHp(participant);
        return participant;
    }

    private void moveTurn(InitiativeTracker tracker, int direction) {
        var participants = participants(tracker).stream().filter(this::isTurnAvailable).toList();
        if (participants.isEmpty()) {
            tracker.setCurrentParticipantId(null);
            return;
        }
        var currentIndex = 0;
        for (int i = 0; i < participants.size(); i++) {
            if (participants.get(i).getId().equals(tracker.getCurrentParticipantId())) {
                currentIndex = i;
                break;
            }
        }
        var nextIndex = currentIndex + direction;
        if (nextIndex >= participants.size()) {
            tracker.setCurrentRound(tracker.getCurrentRound() + 1);
            if (tracker.isRerollEachRound()) {
                rerollRound(tracker);
                participants = participants(tracker).stream().filter(this::isTurnAvailable).toList();
            }
            nextIndex = 0;
        } else if (nextIndex < 0) {
            if (tracker.getCurrentRound() > 1) {
                tracker.setCurrentRound(tracker.getCurrentRound() - 1);
            }
            nextIndex = participants.size() - 1;
        }
        tracker.setCurrentParticipantId(participants.get(nextIndex).getId());
    }

    private void rerollRound(InitiativeTracker tracker) {
        participants(tracker).stream()
                .filter(participant -> participant.getRollMode() != InitiativeRollMode.MANUAL)
                .forEach(this::rollInitiative);
        sortParticipants(tracker);
    }

    private void rollMissingInitiatives(InitiativeTracker tracker) {
        var participants = participants(tracker);
        if (tracker.isGroupSameCreaturesInitiative()) {
            var groupedRolls = participants.stream()
                    .filter(p -> p.getType() == InitiativeParticipantType.CREATURE)
                    .filter(p -> p.getSourceCreature() != null)
                    .filter(p -> p.getInitiativeTotal() == null)
                    .collect(Collectors.groupingBy(p -> p.getSourceCreature().getUrl()));
            groupedRolls.values().forEach(group -> {
                var sample = group.getFirst();
                rollInitiative(sample);
                group.forEach(participant -> copyRoll(sample, participant));
            });
        }
        participants.stream()
                .filter(participant -> participant.getInitiativeTotal() == null)
                .forEach(participant -> {
                    if (participant.getRollMode() == InitiativeRollMode.MANUAL && participant.getRollValue() != null) {
                        applyManualRoll(participant, participant.getRollValue());
                    } else {
                        rollInitiative(participant);
                    }
                });
    }

    private void rollInitiative(InitiativeParticipant participant) {
        var mode = participant.getRollMode() == null ? InitiativeRollMode.NORMAL : participant.getRollMode();
        if (mode == InitiativeRollMode.MANUAL && participant.getRollValue() != null) {
            applyManualRoll(participant, participant.getRollValue());
            return;
        }
        var first = d20();
        var rolls = new ArrayList<Integer>();
        rolls.add(first);
        var selected = first;
        if (mode == InitiativeRollMode.ADVANTAGE || mode == InitiativeRollMode.DISADVANTAGE) {
            var second = d20();
            rolls.add(second);
            selected = mode == InitiativeRollMode.ADVANTAGE ? Math.max(first, second) : Math.min(first, second);
        }
        participant.setRolls(rolls);
        participant.setRollValue(selected);
        participant.setInitiativeTotal(selected + participant.getInitiativeBonus());
    }

    private void applyManualRoll(InitiativeParticipant participant, int rollValue) {
        participant.setRollMode(InitiativeRollMode.MANUAL);
        participant.setRolls(List.of(rollValue));
        participant.setRollValue(rollValue);
        participant.setInitiativeTotal(rollValue + participant.getInitiativeBonus());
    }

    private void copyRoll(InitiativeParticipant from, InitiativeParticipant to) {
        to.setRolls(new ArrayList<>(from.getRolls()));
        to.setRollValue(from.getRollValue());
        to.setInitiativeTotal(from.getInitiativeTotal());
    }

    private void sortParticipants(InitiativeTracker tracker) {
        var sorted = participants(tracker).stream()
                .sorted(Comparator
                        .comparing((InitiativeParticipant p) -> p.getInitiativeTotal() == null ? Integer.MIN_VALUE : p.getInitiativeTotal()).reversed()
                        .thenComparing(Comparator.comparing(InitiativeParticipant::getDexterityBonus).reversed())
                        .thenComparing(InitiativeParticipant::getOrderIndex)
                        .thenComparing(p -> p.getId().toString()))
                .toList();
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setOrderIndex(i);
        }
        participantRepository.saveAll(sorted);
    }

    private EncounterDifficulty calculateDifficulty(InitiativeTracker tracker) {
        var participants = participants(tracker);
        var enemies = participants.stream()
                .filter(p -> p.getRelationType() == InitiativeRelationType.ENEMY)
                .filter(p -> p.getSourceCreature() != null)
                .toList();
        var playerLevels = participants.stream()
                .filter(p -> p.getType() == InitiativeParticipantType.PLAYER)
                .filter(p -> p.getLevel() != null && p.getLevel() > 0)
                .map(InitiativeParticipant::getLevel)
                .toList();

        var difficulty = new EncounterDifficulty();
        difficulty.setEnemyCount(enemies.size());
        difficulty.setPlayerCount(playerLevels.size());
        difficulty.setBaseXp(enemies.stream()
                .map(InitiativeParticipant::getSourceCreature)
                .map(Creature::getExperience)
                .filter(xp -> xp != null && xp > 0)
                .mapToLong(Long::longValue)
                .sum());
        difficulty.setAdjustedXp(Math.round(difficulty.getBaseXp() * encounterMultiplier(enemies.size())));
        var thresholds = new EncounterDifficulty.Thresholds();
        thresholds.setEasy(playerLevels.stream().mapToLong(level -> xpThreshold(level, 0)).sum());
        thresholds.setMedium(playerLevels.stream().mapToLong(level -> xpThreshold(level, 1)).sum());
        thresholds.setHard(playerLevels.stream().mapToLong(level -> xpThreshold(level, 2)).sum());
        thresholds.setDeadly(playerLevels.stream().mapToLong(level -> xpThreshold(level, 3)).sum());
        difficulty.setThresholds(thresholds);
        difficulty.setDifficulty(resolveDifficulty(difficulty.getAdjustedXp(), thresholds));
        return difficulty;
    }

    private EncounterDifficultyLevel resolveDifficulty(long adjustedXp, EncounterDifficulty.Thresholds thresholds) {
        if (adjustedXp >= thresholds.getDeadly() && thresholds.getDeadly() > 0) {
            return EncounterDifficultyLevel.DEADLY;
        }
        if (adjustedXp >= thresholds.getHard() && thresholds.getHard() > 0) {
            return EncounterDifficultyLevel.HARD;
        }
        if (adjustedXp >= thresholds.getMedium() && thresholds.getMedium() > 0) {
            return EncounterDifficultyLevel.MEDIUM;
        }
        if (adjustedXp >= thresholds.getEasy() && thresholds.getEasy() > 0) {
            return EncounterDifficultyLevel.EASY;
        }
        return EncounterDifficultyLevel.TRIVIAL;
    }

    private long xpThreshold(int level, int column) {
        var thresholds = Map.ofEntries(
                Map.entry(1, List.of(25L, 50L, 75L, 100L)),
                Map.entry(2, List.of(50L, 100L, 150L, 200L)),
                Map.entry(3, List.of(75L, 150L, 225L, 400L)),
                Map.entry(4, List.of(125L, 250L, 375L, 500L)),
                Map.entry(5, List.of(250L, 500L, 750L, 1100L)),
                Map.entry(6, List.of(300L, 600L, 900L, 1400L)),
                Map.entry(7, List.of(350L, 750L, 1100L, 1700L)),
                Map.entry(8, List.of(450L, 900L, 1400L, 2100L)),
                Map.entry(9, List.of(550L, 1100L, 1600L, 2400L)),
                Map.entry(10, List.of(600L, 1200L, 1900L, 2800L)),
                Map.entry(11, List.of(800L, 1600L, 2400L, 3600L)),
                Map.entry(12, List.of(1000L, 2000L, 3000L, 4500L)),
                Map.entry(13, List.of(1100L, 2200L, 3400L, 5100L)),
                Map.entry(14, List.of(1250L, 2500L, 3800L, 5700L)),
                Map.entry(15, List.of(1400L, 2800L, 4300L, 6400L)),
                Map.entry(16, List.of(1600L, 3200L, 4800L, 7200L)),
                Map.entry(17, List.of(2000L, 3900L, 5900L, 8800L)),
                Map.entry(18, List.of(2100L, 4200L, 6300L, 9500L)),
                Map.entry(19, List.of(2400L, 4900L, 7300L, 10900L)),
                Map.entry(20, List.of(2800L, 5700L, 8500L, 12700L))
        );
        var normalizedLevel = Math.clamp(level, 1, 20);
        return thresholds.getOrDefault(normalizedLevel, thresholds.get(1)).get(column);
    }

    private double encounterMultiplier(int enemyCount) {
        if (enemyCount <= 1) return 1;
        if (enemyCount == 2) return 1.5;
        if (enemyCount <= 6) return 2;
        if (enemyCount <= 10) return 2.5;
        if (enemyCount <= 14) return 3;
        return 4;
    }

    private void updateStateFromHp(InitiativeParticipant participant) {
        if (participant.getHpCurrent() > 0) {
            participant.setState(InitiativeParticipantState.ACTIVE);
            return;
        }
        participant.setState(participant.getType() == InitiativeParticipantType.CREATURE
                ? InitiativeParticipantState.DEAD
                : InitiativeParticipantState.UNCONSCIOUS);
    }

    private boolean isTurnAvailable(InitiativeParticipant participant) {
        return participant.getState() != InitiativeParticipantState.DEAD;
    }

    private java.util.Optional<InitiativeParticipant> firstAlive(List<InitiativeParticipant> participants) {
        return participants.stream().filter(this::isTurnAvailable).findFirst();
    }

    private int nextOrderIndex(InitiativeTracker tracker) {
        return participants(tracker).stream()
                .mapToInt(InitiativeParticipant::getOrderIndex)
                .max()
                .orElse(-1) + 1;
    }

    private InitiativeTracker findOwnedTracker(UUID trackerId) {
        return trackerRepository.findByIdAndOwnerId(trackerId, currentOwnerId())
                .orElseThrow(() -> new EntityNotFoundException("Initiative tracker not found"));
    }

    private UUID currentOwnerId() {
        var ownerId = SecurityUtils.getUser().getUuid();
        if (ownerId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User token does not contain user id");
        }
        return ownerId;
    }

    private InitiativeParticipant findParticipant(InitiativeTracker tracker, UUID participantId) {
        return participants(tracker).stream()
                .filter(participant -> participant.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Initiative participant not found"));
    }

    private List<InitiativeParticipant> participants(InitiativeTracker tracker) {
        return participantRepository.findAllByTrackerOrderByOrderIndexAsc(tracker);
    }

    private int d20() {
        return RANDOM.nextInt(20) + 1;
    }

    private int creatureHp(Creature creature) {
        return creature.getHit() == null || creature.getHit().getValue() == null ? 0 : creature.getHit().getValue();
    }

    private int creatureDexterityBonus(Creature creature) {
        return creature.getAbilities() == null ? 0 : creature.getAbilities().getMod(Ability.DEXTERITY);
    }

    private int creatureInitiativeBonus(Creature creature) {
        if (creature.getInitiative() == null) {
            return creatureDexterityBonus(creature);
        }
        return creatureDexterityBonus(creature) + ChallengeRating.getPb(creature.getExperience()) * creature.getInitiative().getMultiplier();
    }

    private InitiativeTrackerResponse toResponse(InitiativeTracker tracker) {
        return InitiativeTrackerResponse.builder()
                .id(tracker.getId())
                .title(tracker.getTitle())
                .status(tracker.getStatus())
                .currentRound(tracker.getCurrentRound())
                .currentParticipantId(tracker.getCurrentParticipantId())
                .rerollEachRound(tracker.isRerollEachRound())
                .groupSameCreaturesInitiative(tracker.isGroupSameCreaturesInitiative())
                .shareToken(tracker.getShareToken())
                .encounterDifficulty(tracker.getEncounterDifficulty())
                .participants(participants(tracker).stream().map(this::toResponse).toList())
                .createdAt(tracker.getCreatedAt())
                .updatedAt(tracker.getUpdatedAt())
                .build();
    }

    private InitiativeParticipantResponse toResponse(InitiativeParticipant participant) {
        return InitiativeParticipantResponse.builder()
                .id(participant.getId())
                .type(participant.getType())
                .relationType(participant.getRelationType())
                .name(participant.getName())
                .baseName(participant.getBaseName())
                .displayName(participant.getDisplayName())
                .level(participant.getLevel())
                .sourceCreatureId(participant.getSourceCreature() == null ? null : participant.getSourceCreature().getUrl())
                .sameCreatureIndex(participant.getSameCreatureIndex())
                .hpMax(participant.getHpMax())
                .hpCurrent(participant.getHpCurrent())
                .hpTemporary(participant.getHpTemporary())
                .state(participant.getState())
                .initiativeBonus(participant.getInitiativeBonus())
                .dexterityBonus(participant.getDexterityBonus())
                .rollMode(participant.getRollMode())
                .rolls(participant.getRolls())
                .rollValue(participant.getRollValue())
                .initiativeTotal(participant.getInitiativeTotal())
                .orderIndex(participant.getOrderIndex())
                .addedRound(participant.getAddedRound())
                .createdAt(participant.getCreatedAt())
                .updatedAt(participant.getUpdatedAt())
                .build();
    }

    private ActiveParticipantResponse activeResponse(InitiativeTracker tracker) {
        var participantById = participants(tracker).stream()
                .collect(Collectors.toMap(InitiativeParticipant::getId, Function.identity()));
        var participant = participantById.get(tracker.getCurrentParticipantId());
        return ActiveParticipantResponse.builder()
                .participant(participant == null ? null : toResponse(participant))
                .statBlock(participant == null || participant.getSourceCreature() == null ? null : creatureMapper.toDetail(participant.getSourceCreature()))
                .build();
    }
}
