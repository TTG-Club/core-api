package club.ttg.dnd5.domain.tool.tracker.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbilities;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbility;
import club.ttg.dnd5.domain.beastiary.model.CreatureInitiative;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.tool.tracker.model.InitiativeParticipant;
import club.ttg.dnd5.domain.tool.tracker.model.InitiativeTracker;
import club.ttg.dnd5.domain.tool.tracker.model.ParticipantType;
import club.ttg.dnd5.domain.tool.tracker.model.TrackerStatus;
import club.ttg.dnd5.domain.tool.tracker.repository.InitiativeParticipantRepository;
import club.ttg.dnd5.domain.tool.tracker.repository.InitiativeTrackerRepository;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.ParticipantAddRequest;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.ParticipantUpdateRequest;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.TrackerRequest;
import club.ttg.dnd5.domain.tool.tracker.rest.mapper.InitiativeTrackerMapper;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InitiativeTrackerServiceTest {

    private final InitiativeTrackerRepository trackerRepository = mock(InitiativeTrackerRepository.class);
    private final InitiativeParticipantRepository participantRepository = mock(InitiativeParticipantRepository.class);
    private final TrackerCreationRateLimiter creationRateLimiter = mock(TrackerCreationRateLimiter.class);
    private final CreatureRepository creatureRepository = mock(CreatureRepository.class);
    private final InitiativeTrackerMapper trackerMapper = mock(InitiativeTrackerMapper.class);
    private final InitiativeTrackerService service = new InitiativeTrackerService(
            trackerRepository,
            participantRepository,
            new InitiativeCombatService(),
            creationRateLimiter,
            creatureRepository,
            trackerMapper
    );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void anonymousCreateChecksRateLimitAndHasNoOwner() {
        when(trackerRepository.saveAndFlush(any(InitiativeTracker.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(new TrackerRequest("Логово дракона", null), "10.0.0.1");

        verify(creationRateLimiter).checkAnonymousCreation("10.0.0.1");
        ArgumentCaptor<InitiativeTracker> captor = ArgumentCaptor.forClass(InitiativeTracker.class);
        verify(trackerRepository).saveAndFlush(captor.capture());
        InitiativeTracker saved = captor.getValue();
        assertNull(saved.getOwnerUsername());
        assertNotNull(saved.getAccessKey());
        assertEquals("Логово дракона", saved.getName());
        assertEquals(TrackerStatus.PREPARING, saved.getStatus());
        // Ключ доступа отдаётся только в ответе на создание
        verify(trackerMapper).toCreatedResponse(saved, List.of());
    }

    @Test
    void authorizedCreateSetsOwnerAndSkipsRateLimit() {
        authenticate("dungeon-master");
        when(trackerRepository.countByOwnerUsernameAndDeletedFalse("dungeon-master")).thenReturn(9L);
        when(trackerRepository.saveAndFlush(any(InitiativeTracker.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(null, "10.0.0.1");

        verify(creationRateLimiter, never()).checkAnonymousCreation(anyString());
        ArgumentCaptor<InitiativeTracker> captor = ArgumentCaptor.forClass(InitiativeTracker.class);
        verify(trackerRepository).saveAndFlush(captor.capture());
        assertEquals("dungeon-master", captor.getValue().getOwnerUsername());
        assertEquals("Новый трекер", captor.getValue().getName());
    }

    @Test
    void authorizedCreateFailsOnTrackerLimit() {
        authenticate("dungeon-master");
        when(trackerRepository.countByOwnerUsernameAndDeletedFalse("dungeon-master")).thenReturn(10L);

        ApiException exception =
                assertThrows(ApiException.class, () -> service.create(null, "10.0.0.1"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void anonymousTrackerRequiresMatchingKey() {
        InitiativeTracker tracker = anonymousTracker();
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.findById(tracker.getId(), UUID.randomUUID().toString()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void anonymousTrackerOpensByKey() {
        InitiativeTracker tracker = anonymousTracker();
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));
        when(participantRepository.findAllByTrackerId(tracker.getId())).thenReturn(List.of());

        service.findById(tracker.getId(), tracker.getAccessKey().toString());

        verify(trackerMapper).toDetailedResponse(tracker, List.of());
    }

    @Test
    void ownedTrackerDeniedForAnotherUser() {
        authenticate("intruder");
        InitiativeTracker tracker = ownedTracker("dungeon-master");
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.findById(tracker.getId(), tracker.getAccessKey().toString()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void ownedTrackerDeniedForAnonymousEvenWithKey() {
        InitiativeTracker tracker = ownedTracker("dungeon-master");
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.findById(tracker.getId(), tracker.getAccessKey().toString()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void addPlayerRequiresName() {
        InitiativeTracker tracker = anonymousTracker();
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));

        ParticipantAddRequest request = new ParticipantAddRequest();
        request.setType(ParticipantType.PLAYER);

        assertThrows(ApiException.class,
                () -> service.addParticipants(tracker.getId(), request, tracker.getAccessKey().toString()));
    }

    @Test
    void addPlayerFailsOnLimit() {
        InitiativeTracker tracker = anonymousTracker();
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));
        when(participantRepository.countByTrackerIdAndType(tracker.getId(), ParticipantType.PLAYER))
                .thenReturn(50L);

        ParticipantAddRequest request = new ParticipantAddRequest();
        request.setType(ParticipantType.PLAYER);
        request.setName("Бард");

        assertThrows(ApiException.class,
                () -> service.addParticipants(tracker.getId(), request, tracker.getAccessKey().toString()));
    }

    @Test
    void addCreaturesFailsWhenBatchExceedsLimit() {
        InitiativeTracker tracker = anonymousTracker();
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));
        when(participantRepository.countByTrackerIdAndType(tracker.getId(), ParticipantType.CREATURE))
                .thenReturn(95L);

        ParticipantAddRequest request = new ParticipantAddRequest();
        request.setType(ParticipantType.CREATURE);
        request.setCreatureUrl("goblin");
        request.setCount(6);

        assertThrows(ApiException.class,
                () -> service.addParticipants(tracker.getId(), request, tracker.getAccessKey().toString()));
    }

    @Test
    void addCreaturesSnapshotsBonusAndNumbersNamesAfterMaxSuffix() {
        InitiativeTracker tracker = anonymousTracker();
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));
        when(participantRepository.countByTrackerIdAndType(tracker.getId(), ParticipantType.CREATURE))
                .thenReturn(0L);
        // «Гоблин 2» удалён: нумерация должна продолжиться после максимального суффикса, без дублей
        when(participantRepository.findAllByTrackerId(tracker.getId())).thenReturn(List.of(
                creatureParticipant("Гоблин 1", "goblin", 5),
                creatureParticipant("Гоблин 3", "goblin", 7)));
        when(creatureRepository.findById("goblin")).thenReturn(Optional.of(goblin()));

        ParticipantAddRequest request = new ParticipantAddRequest();
        request.setType(ParticipantType.CREATURE);
        request.setCreatureUrl("goblin");
        request.setCount(2);

        service.addParticipants(tracker.getId(), request, tracker.getAccessKey().toString());

        ArgumentCaptor<List<InitiativeParticipant>> captor = ArgumentCaptor.forClass(List.class);
        verify(participantRepository).saveAll(captor.capture());
        List<InitiativeParticipant> saved = captor.getValue();
        assertEquals(2, saved.size());
        // ЛОВ 14 (+2), множитель 0 → бонус 2; максимальный суффикс 3 → новые 4 и 5
        assertEquals("Гоблин 4", saved.getFirst().getName());
        assertEquals("Гоблин 5", saved.getLast().getName());
        assertEquals(2, saved.getFirst().getInitiativeBonus());
        assertEquals("goblin", saved.getFirst().getCreatureUrl());
        assertEquals(8, saved.getFirst().getSeq());
        assertEquals(9, saved.getLast().getSeq());
        // В подготовке инициатива новичкам не бросается
        assertNull(saved.getFirst().getInitiativeRoll());
    }

    @Test
    void addParticipantToActiveCombatRollsInitiative() {
        InitiativeTracker tracker = anonymousTracker();
        tracker.setStatus(TrackerStatus.ACTIVE);
        tracker.setRound(2);
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));
        when(participantRepository.countByTrackerIdAndType(tracker.getId(), ParticipantType.PLAYER))
                .thenReturn(0L);
        when(participantRepository.findMaxSeq(tracker.getId())).thenReturn(0);

        ParticipantAddRequest request = new ParticipantAddRequest();
        request.setType(ParticipantType.PLAYER);
        request.setName("Паладин");
        request.setInitiativeBonus(1);

        service.addParticipants(tracker.getId(), request, tracker.getAccessKey().toString());

        ArgumentCaptor<List<InitiativeParticipant>> captor = ArgumentCaptor.forClass(List.class);
        verify(participantRepository).saveAll(captor.capture());
        InitiativeParticipant saved = captor.getValue().getFirst();
        assertNotNull(saved.getInitiativeRoll());
        assertEquals(saved.getInitiativeRoll() + 1, saved.getInitiativeTotal());
    }

    @Test
    void deleteAnonymousTrackerIsPhysical() {
        InitiativeTracker tracker = anonymousTracker();
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));

        service.delete(tracker.getId(), tracker.getAccessKey().toString());

        verify(trackerRepository).delete(tracker);
    }

    @Test
    void deleteOwnedTrackerIsSoftAndPurgesParticipants() {
        authenticate("dungeon-master");
        InitiativeTracker tracker = ownedTracker("dungeon-master");
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));
        when(trackerRepository.findAllByOwnerUsernameAndDeletedTrueOrderByUpdatedAtDesc("dungeon-master"))
                .thenReturn(List.of(tracker));

        service.delete(tracker.getId(), null);

        verify(trackerRepository, never()).delete(tracker);
        assertEquals(true, tracker.isDeleted());
        // Участники мягко удалённого трекера недоступны — физически чистятся сразу
        verify(participantRepository).deleteAllByTrackerId(tracker.getId());
        verify(trackerRepository, never()).deleteAll(any());
    }

    @Test
    void deleteOwnedTrackerTrimsHistoryBeyondLimit() {
        authenticate("dungeon-master");
        InitiativeTracker tracker = ownedTracker("dungeon-master");
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));
        List<InitiativeTracker> deletedHistory = new java.util.ArrayList<>();
        for (int i = 0; i < 31; i++) {
            deletedHistory.add(ownedTracker("dungeon-master"));
        }
        when(trackerRepository.findAllByOwnerUsernameAndDeletedTrueOrderByUpdatedAtDesc("dungeon-master"))
                .thenReturn(deletedHistory);

        service.delete(tracker.getId(), null);

        // История ограничена 30 удалёнными: самый старый (31-й) удаляется физически
        ArgumentCaptor<List<InitiativeTracker>> captor = ArgumentCaptor.forClass(List.class);
        verify(trackerRepository).deleteAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(deletedHistory.getLast(), captor.getValue().getFirst());
    }

    @Test
    void findMineRequiresAuthorization() {
        ApiException exception = assertThrows(ApiException.class, () -> service.findMine(false));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void updateParticipantMarksDead() {
        InitiativeTracker tracker = anonymousTracker();
        InitiativeParticipant participant = creatureParticipant("Гоблин 1", "goblin", 1);
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));
        when(participantRepository.findByIdAndTrackerId(participant.getId(), tracker.getId()))
                .thenReturn(Optional.of(participant));
        when(participantRepository.findAllByTrackerId(tracker.getId())).thenReturn(List.of(participant));

        ParticipantUpdateRequest request = new ParticipantUpdateRequest();
        request.setDead(true);

        service.updateParticipant(tracker.getId(), participant.getId(), request, tracker.getAccessKey().toString());

        assertTrue(participant.isDead());
    }

    @Test
    void updateSettingsTogglesRerollEachRoundWithoutResettingName() {
        InitiativeTracker tracker = anonymousTracker();
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));
        when(participantRepository.findAllByTrackerId(tracker.getId())).thenReturn(List.of());

        service.updateSettings(tracker.getId(), new TrackerRequest(null, true), tracker.getAccessKey().toString());

        assertTrue(tracker.isRerollEachRound());
        // Имя не передано (null) — не должно сброситься в дефолт
        assertEquals("Трекер", tracker.getName());
    }

    @Test
    void rollParticipantRollsSingleWithBonus() {
        InitiativeTracker tracker = anonymousTracker();
        InitiativeParticipant participant = creatureParticipant("Гоблин 1", "goblin", 1);
        participant.setInitiativeBonus(2);
        when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));
        when(participantRepository.findByIdAndTrackerId(participant.getId(), tracker.getId()))
                .thenReturn(Optional.of(participant));
        when(participantRepository.findAllByTrackerId(tracker.getId())).thenReturn(List.of(participant));

        service.rollParticipant(tracker.getId(), participant.getId(), tracker.getAccessKey().toString());

        assertNotNull(participant.getInitiativeRoll());
        assertEquals(participant.getInitiativeRoll() + 2, participant.getInitiativeTotal());
    }

    private static void authenticate(String username) {
        User user = new User();
        user.setUsername(username);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    private static InitiativeTracker anonymousTracker() {
        InitiativeTracker tracker = new InitiativeTracker();
        tracker.setId(UUID.randomUUID());
        tracker.setName("Трекер");
        tracker.setAccessKey(UUID.randomUUID());
        tracker.setStatus(TrackerStatus.PREPARING);
        return tracker;
    }

    private static InitiativeTracker ownedTracker(String ownerUsername) {
        InitiativeTracker tracker = anonymousTracker();
        tracker.setOwnerUsername(ownerUsername);
        return tracker;
    }

    private static InitiativeParticipant creatureParticipant(String name, String creatureUrl, int seq) {
        InitiativeParticipant participant = new InitiativeParticipant();
        participant.setId(UUID.randomUUID());
        participant.setType(ParticipantType.CREATURE);
        participant.setName(name);
        participant.setCreatureUrl(creatureUrl);
        participant.setSeq(seq);
        return participant;
    }

    private static Creature goblin() {
        CreatureAbility dex = new CreatureAbility();
        dex.setValue((short) 14);

        CreatureAbilities abilities = new CreatureAbilities();
        abilities.setDexterity(dex);

        CreatureInitiative initiative = new CreatureInitiative();
        initiative.setMultiplier((byte) 0);

        Creature creature = new Creature();
        creature.setUrl("goblin");
        creature.setName("Гоблин");
        creature.setAbilities(abilities);
        creature.setInitiative(initiative);
        creature.setExperience(50L);
        return creature;
    }
}
