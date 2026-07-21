package club.ttg.dnd5.domain.spellbook.service;

import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.mapper.SpellMapper;
import club.ttg.dnd5.domain.spellbook.model.Spellbook;
import club.ttg.dnd5.domain.spellbook.model.SpellbookAccess;
import club.ttg.dnd5.domain.spellbook.model.SpellbookSpell;
import club.ttg.dnd5.domain.spellbook.repository.SpellbookAccessRepository;
import club.ttg.dnd5.domain.spellbook.repository.SpellbookRepository;
import club.ttg.dnd5.domain.spellbook.repository.SpellbookSpellRepository;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookAddSpellsRequest;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookLevelGroupResponse;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookRequest;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookSpellUpdateRequest;
import club.ttg.dnd5.domain.spellbook.rest.mapper.SpellbookMapper;
import club.ttg.dnd5.domain.subscription.service.SubscriptionStatusClient;
import club.ttg.dnd5.domain.subscription.service.SubscriptionStatusClient.SubscriptionStatus;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpellbookServiceTest {

    private static final String OWNER = "wizard";

    private final SpellbookRepository spellbookRepository = mock(SpellbookRepository.class);
    private final SpellbookSpellRepository spellbookSpellRepository = mock(SpellbookSpellRepository.class);
    private final SpellbookAccessRepository accessRepository = mock(SpellbookAccessRepository.class);
    private final SpellRepository spellRepository = mock(SpellRepository.class);
    private final SubscriptionStatusClient subscriptionStatusClient =
            mock(SubscriptionStatusClient.class);

    private final SpellbookMapper spellbookMapper = mock(SpellbookMapper.class);
    private final SpellMapper spellMapper = mock(SpellMapper.class);
    private final SpellbookService service = new SpellbookService(
            spellbookRepository,
            spellbookSpellRepository,
            accessRepository,
            spellRepository,
            subscriptionStatusClient,
            spellbookMapper,
            spellMapper
    );

    /** Ответ subscriber-service: подписка действует / её нет (в т.ч. fail-closed). */
    private void withSubscription(boolean active) {
        when(subscriptionStatusClient.fetch(anyString()))
                .thenReturn(new SubscriptionStatus(active, active, null, null, null));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void anonymousCannotCreateSpellbook() {
        ApiException exception = assertThrows(ApiException.class, () -> service.create(null));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verify(spellbookRepository, never()).saveAndFlush(any(Spellbook.class));
    }

    @Test
    void createSetsOwnerAndDefaultName() {
        authenticate(OWNER);
        withSubscription(false);
        when(spellbookRepository.countByOwnerUsername(OWNER)).thenReturn(2L);
        when(spellbookRepository.saveAndFlush(any(Spellbook.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(null);

        ArgumentCaptor<Spellbook> captor = ArgumentCaptor.forClass(Spellbook.class);
        verify(spellbookRepository).saveAndFlush(captor.capture());
        assertEquals(OWNER, captor.getValue().getOwnerUsername());
        assertEquals("Новая книга заклинаний", captor.getValue().getName());
    }

    @Test
    void freeUserIsLimitedToThreeSpellbooks() {
        authenticate(OWNER);
        when(spellbookRepository.countByOwnerUsername(OWNER)).thenReturn(3L);
        withSubscription(false);

        ApiException exception = assertThrows(ApiException.class, () -> service.create(null));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(spellbookRepository, never()).saveAndFlush(any(Spellbook.class));
    }

    @Test
    void subscriberIsNotLimited() {
        authenticate(OWNER);
        withSubscription(true);
        when(spellbookRepository.saveAndFlush(any(Spellbook.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(new SpellbookRequest("Книга ученика", null));

        ArgumentCaptor<Spellbook> captor = ArgumentCaptor.forClass(Spellbook.class);
        verify(spellbookRepository).saveAndFlush(captor.capture());
        assertEquals("Книга ученика", captor.getValue().getName());
        verify(spellbookRepository, never()).countByOwnerUsername(anyString());
    }

    @Test
    void createGeneratesShareKey() {
        authenticate(OWNER);
        withSubscription(false);
        when(spellbookRepository.saveAndFlush(any(Spellbook.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(null);

        ArgumentCaptor<Spellbook> captor = ArgumentCaptor.forClass(Spellbook.class);
        verify(spellbookRepository).saveAndFlush(captor.capture());
        assertNotNull(captor.getValue().getShareKey());
    }

    @Test
    void subscriberServiceFailureFallsBackToFreeLimit() {
        authenticate(OWNER);
        // fail-closed: недоступный subscriber-service отдаёт denied() — подписки как бы нет
        when(subscriptionStatusClient.fetch(anyString()))
                .thenReturn(SubscriptionStatus.denied());
        when(spellbookRepository.countByOwnerUsername(OWNER)).thenReturn(3L);

        ApiException exception = assertThrows(ApiException.class, () -> service.create(null));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void foreignSpellbookIsNotFoundWithoutAccess() {
        authenticate("intruder");
        Spellbook spellbook = spellbook();
        when(spellbookRepository.findById(spellbook.getId())).thenReturn(Optional.of(spellbook));
        when(accessRepository.existsBySpellbookIdAndUserUsername(spellbook.getId(), "intruder"))
                .thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.findById(spellbook.getId()));
    }

    @Test
    void sharedSpellbookIsReadableButHidesShareKey() {
        authenticate("reader");
        Spellbook spellbook = spellbook();
        when(spellbookRepository.findById(spellbook.getId())).thenReturn(Optional.of(spellbook));
        when(accessRepository.existsBySpellbookIdAndUserUsername(spellbook.getId(), "reader"))
                .thenReturn(true);
        when(spellbookSpellRepository.findAllBySpellbookId(spellbook.getId())).thenReturn(List.of());

        service.findById(spellbook.getId());

        verify(spellbookMapper).toSharedDetailedResponse(spellbook, List.of(), 0L, 0L);
        verify(spellbookMapper, never()).toOwnDetailedResponse(any(), anyList(), anyLong(), anyLong());
    }

    @Test
    void sharedSpellbookIsNotEditableByReader() {
        authenticate("reader");
        Spellbook spellbook = spellbook();
        when(spellbookRepository.findByIdAndOwnerUsername(spellbook.getId(), "reader"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateSpell(spellbook.getId(), "shield", new SpellbookSpellUpdateRequest(true)));
    }

    @Test
    void linkAddsSpellbookToAvailableOnce() {
        authenticate("reader");
        Spellbook spellbook = spellbook();
        when(spellbookRepository.findByShareKey(spellbook.getShareKey())).thenReturn(Optional.of(spellbook));
        when(accessRepository.existsBySpellbookIdAndUserUsername(spellbook.getId(), "reader"))
                .thenReturn(false, true);
        when(spellbookSpellRepository.findAllBySpellbookId(spellbook.getId())).thenReturn(List.of());

        service.addShared(spellbook.getShareKey());
        service.addShared(spellbook.getShareKey());

        ArgumentCaptor<SpellbookAccess> captor = ArgumentCaptor.forClass(SpellbookAccess.class);
        verify(accessRepository).save(captor.capture());
        assertEquals(spellbook.getId(), captor.getValue().getSpellbookId());
        assertEquals("reader", captor.getValue().getUserUsername());
    }

    @Test
    void ownerFollowingOwnLinkGetsNoAccessRow() {
        authenticate(OWNER);
        Spellbook spellbook = spellbook();
        when(spellbookRepository.findByShareKey(spellbook.getShareKey())).thenReturn(Optional.of(spellbook));
        when(spellbookSpellRepository.findAllBySpellbookId(spellbook.getId())).thenReturn(List.of());

        service.addShared(spellbook.getShareKey());

        verify(accessRepository, never()).save(any(SpellbookAccess.class));
        verify(spellbookMapper).toOwnDetailedResponse(spellbook, List.of(), 0L, 0L);
    }

    @Test
    void deleteSharedRemovesOnlyOwnAccess() {
        authenticate("reader");
        UUID spellbookId = UUID.randomUUID();
        when(accessRepository.findBySpellbookIdAndUserUsername(spellbookId, "reader"))
                .thenReturn(Optional.of(new SpellbookAccess()));

        service.deleteShared(spellbookId);

        verify(accessRepository).deleteBySpellbookIdAndUserUsername(spellbookId, "reader");
        verify(spellbookRepository, never()).delete(any(Spellbook.class));
    }

    @Test
    void deleteSharedFailsWithoutAccess() {
        authenticate("reader");
        UUID spellbookId = UUID.randomUUID();
        when(accessRepository.findBySpellbookIdAndUserUsername(spellbookId, "reader"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteShared(spellbookId));
    }

    @Test
    void listSplitsOwnAndSharedSpellbooks() {
        authenticate(OWNER);
        Spellbook own = spellbook();
        Spellbook shared = spellbook();
        shared.setOwnerUsername("another-wizard");
        SpellbookAccess access = new SpellbookAccess();
        access.setSpellbookId(shared.getId());
        access.setUserUsername(OWNER);
        when(spellbookRepository.findAllByOwnerUsernameOrderByCreatedAtDesc(OWNER)).thenReturn(List.of(own));
        when(accessRepository.findAllByUserUsernameOrderByCreatedAtDesc(OWNER)).thenReturn(List.of(access));
        when(spellbookRepository.findAllById(List.of(shared.getId()))).thenReturn(List.of(shared));

        service.findMine();

        verify(spellbookMapper).toOwnShortResponse(own, 0L, 0L);
        verify(spellbookMapper).toSharedShortResponse(shared, 0L, 0L);
    }

    @Test
    void addSpellsRejectsUnknownSpell() {
        authenticate(OWNER);
        Spellbook spellbook = spellbook();
        when(spellbookRepository.findByIdAndOwnerUsername(spellbook.getId(), OWNER))
                .thenReturn(Optional.of(spellbook));
        when(spellRepository.findVisibleUrls(anyCollection())).thenReturn(Set.of("magic-missile"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> service.addSpells(spellbook.getId(),
                        new SpellbookAddSpellsRequest(Set.of("magic-missile", "no-such-spell"), null)));

        assertTrue(exception.getMessage().contains("no-such-spell"));
        verify(spellbookSpellRepository, never()).saveAll(anyCollection());
    }

    @Test
    void addSpellsSkipsAlreadyAddedAndTouchesSpellbook() {
        authenticate(OWNER);
        Spellbook spellbook = spellbook();
        when(spellbookRepository.findByIdAndOwnerUsername(spellbook.getId(), OWNER))
                .thenReturn(Optional.of(spellbook));
        when(spellRepository.findVisibleUrls(anyCollection())).thenReturn(Set.of("magic-missile", "shield"));
        when(spellbookSpellRepository.findSpellUrlsBySpellbookId(spellbook.getId()))
                .thenReturn(Set.of("magic-missile"));
        when(spellbookSpellRepository.findAllBySpellbookId(spellbook.getId())).thenReturn(List.of());

        service.addSpells(spellbook.getId(),
                new SpellbookAddSpellsRequest(Set.of("magic-missile", "shield"), true));

        ArgumentCaptor<List<SpellbookSpell>> captor = ArgumentCaptor.forClass(List.class);
        verify(spellbookSpellRepository).saveAll(captor.capture());
        List<SpellbookSpell> saved = captor.getValue();
        assertEquals(1, saved.size());
        assertEquals("shield", saved.getFirst().getSpellUrl());
        assertTrue(saved.getFirst().isPrepared());
        verify(spellbookRepository).touch(any(UUID.class), any(Instant.class));
    }

    @Test
    void spellsAreGroupedByLevelWithPreparedCounts() {
        authenticate(OWNER);
        Spellbook spellbook = spellbook();
        when(spellbookRepository.findById(spellbook.getId())).thenReturn(Optional.of(spellbook));
        when(spellbookSpellRepository.findAllBySpellbookId(spellbook.getId())).thenReturn(List.of(
                entry("light", false),
                entry("magic-missile", true),
                entry("shield", false)));
        when(spellRepository.findAllShortByUrls(anyCollection())).thenReturn(List.of(
                spell("light", 0L),
                spell("magic-missile", 1L),
                spell("shield", 1L)));
        when(spellMapper.toShort(any(Spell.class))).thenReturn(new SpellShortResponse());

        service.findById(spellbook.getId());

        ArgumentCaptor<List<SpellbookLevelGroupResponse>> captor = ArgumentCaptor.forClass(List.class);
        verify(spellbookMapper).toOwnDetailedResponse(any(Spellbook.class), captor.capture(), anyLong(), anyLong());
        List<SpellbookLevelGroupResponse> levels = captor.getValue();
        assertEquals(2, levels.size());
        assertEquals(0, levels.getFirst().getLevel());
        assertEquals("Заговоры", levels.getFirst().getLevelName());
        assertEquals(1, levels.getFirst().getSpellCount());
        assertEquals(0, levels.getFirst().getPreparedCount());
        assertEquals(1, levels.getLast().getLevel());
        assertEquals("1 уровень", levels.getLast().getLevelName());
        assertEquals(2, levels.getLast().getSpellCount());
        assertEquals(1, levels.getLast().getPreparedCount());
        // Итоговые счётчики книги складываются из групп
        verify(spellbookMapper).toOwnDetailedResponse(spellbook, levels, 3L, 1L);
    }

    @Test
    void preparedFlagIsUpdatedForSpellInSpellbook() {
        authenticate(OWNER);
        Spellbook spellbook = spellbook();
        SpellbookSpell entry = entry("shield", false);
        when(spellbookRepository.findByIdAndOwnerUsername(spellbook.getId(), OWNER))
                .thenReturn(Optional.of(spellbook));
        when(spellbookSpellRepository.findBySpellbookIdAndSpellUrl(spellbook.getId(), "shield"))
                .thenReturn(Optional.of(entry));
        when(spellbookSpellRepository.findAllBySpellbookId(spellbook.getId())).thenReturn(List.of(entry));
        when(spellRepository.findAllShortByUrls(anyCollection())).thenReturn(List.of(spell("shield", 1L)));
        when(spellMapper.toShort(any(Spell.class))).thenReturn(new SpellShortResponse());

        service.updateSpell(spellbook.getId(), "shield", new SpellbookSpellUpdateRequest(true));

        assertTrue(entry.isPrepared());
    }

    @Test
    void preparedFlagFailsForSpellOutsideSpellbook() {
        authenticate(OWNER);
        Spellbook spellbook = spellbook();
        when(spellbookRepository.findByIdAndOwnerUsername(spellbook.getId(), OWNER))
                .thenReturn(Optional.of(spellbook));
        when(spellbookSpellRepository.findBySpellbookIdAndSpellUrl(spellbook.getId(), "fireball"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateSpell(spellbook.getId(), "fireball", new SpellbookSpellUpdateRequest(true)));
    }

    @Test
    void deleteRemovesSpellbookWithItsSpells() {
        authenticate(OWNER);
        Spellbook spellbook = spellbook();
        when(spellbookRepository.findByIdAndOwnerUsername(spellbook.getId(), OWNER))
                .thenReturn(Optional.of(spellbook));

        service.delete(spellbook.getId());

        verify(spellbookSpellRepository).deleteAllBySpellbookId(spellbook.getId());
        verify(spellbookRepository).delete(spellbook);
    }

    private static void authenticate(String username) {
        User user = new User();
        user.setUsername(username);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    private static Spellbook spellbook() {
        Spellbook spellbook = new Spellbook();
        spellbook.setId(UUID.randomUUID());
        spellbook.setName("Книга ученика");
        spellbook.setOwnerUsername(OWNER);
        spellbook.setShareKey(UUID.randomUUID());
        return spellbook;
    }

    private static SpellbookSpell entry(String spellUrl, boolean prepared) {
        SpellbookSpell entry = new SpellbookSpell();
        entry.setId(UUID.randomUUID());
        entry.setSpellUrl(spellUrl);
        entry.setPrepared(prepared);
        return entry;
    }

    private static Spell spell(String url, Long level) {
        Spell spell = new Spell();
        spell.setUrl(url);
        spell.setLevel(level);
        return spell;
    }
}
