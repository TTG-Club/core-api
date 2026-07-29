package club.ttg.dnd5.domain.tool.sheet.service;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.repository.CharacterSheetRepository;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetListResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetPublicResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetRequest;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetShareResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.mapper.CharacterSheetMapper;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Доступ к листу по ссылке (выпуск и отзыв токена владельцем, анонимный просмотр) и лимиты,
 * зависящие от подписки.
 */
class CharacterSheetServiceTest {

    private static final SheetLimits FREE_LIMITS = new SheetLimits(8, 16, 20, 20);
    private static final SheetLimits SUBSCRIBER_LIMITS = new SheetLimits(20, 40, 30, 30);

    private final CharacterSheetRepository sheetRepository = mock(CharacterSheetRepository.class);
    private final CharacterSheetMapper sheetMapper = mock(CharacterSheetMapper.class);
    private final CharacterSheetLimits sheetLimits = mock(CharacterSheetLimits.class);
    private final CharacterSheetService service =
            new CharacterSheetService(sheetRepository, sheetMapper, sheetLimits);

    @BeforeEach
    void withoutSubscriptionByDefault() {
        when(sheetLimits.forUser(any())).thenReturn(FREE_LIMITS);
        when(sheetLimits.subscriberLimits()).thenReturn(SUBSCRIBER_LIMITS);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createOverBaseLimitIsRejected() {
        UUID owner = authenticate();
        when(sheetRepository.countByUserIdAndDeletedFalse(owner)).thenReturn(8L);

        ApiException exception = assertThrows(ApiException.class, () -> service.create(request()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(sheetRepository, never()).saveAndFlush(any());
    }

    @Test
    void subscriberCreatesSheetsBeyondBaseLimit() {
        UUID owner = authenticate();
        when(sheetLimits.forUser(any())).thenReturn(SUBSCRIBER_LIMITS);
        when(sheetRepository.countByUserIdAndDeletedFalse(owner)).thenReturn(8L);
        when(sheetRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(request());

        verify(sheetRepository).saveAndFlush(any());
    }

    @Test
    void findMineReportsSubscriberLimits() {
        UUID owner = authenticate();
        when(sheetLimits.forUser(any())).thenReturn(SUBSCRIBER_LIMITS);
        when(sheetRepository.findAllByUserIdAndDeletedFalseOrderByCreatedAtDesc(owner))
                .thenReturn(List.of(sheet(owner)));

        CharacterSheetListResponse response = service.findMine(false);

        assertEquals(20, response.getLimit());
        assertEquals(30, response.getHistoryLimit());
        assertEquals(1, response.getCount());
        // Лимит подписчика совпал с выданным — клиенту нечего предлагать
        assertEquals(response.getLimit(), response.getSubscriberLimit());
        assertEquals(response.getHistoryLimit(), response.getSubscriberHistoryLimit());
    }

    @Test
    void findMineReportsWhatSubscriptionWouldGive() {
        UUID owner = authenticate();
        when(sheetRepository.findAllByUserIdAndDeletedFalseOrderByCreatedAtDesc(owner))
                .thenReturn(List.of(sheet(owner)));

        CharacterSheetListResponse response = service.findMine(false);

        // Лимит подписчика выше выданного — клиент покажет подсказку про подписку
        assertEquals(8, response.getLimit());
        assertEquals(20, response.getSubscriberLimit());
        assertEquals(20, response.getHistoryLimit());
        assertEquals(30, response.getSubscriberHistoryLimit());
    }

    @Test
    void deleteTrimsHistoryByTrimDepthSoOutageDoesNotWipeIt() {
        UUID owner = authenticate();
        CharacterSheet sheet = sheet(owner);
        when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));
        // Статус подписки неизвестен: показываем базовые 20, но подрезаем по 30
        when(sheetLimits.forUser(any())).thenReturn(new SheetLimits(8, 16, 20, 30));
        List<CharacterSheet> deleted = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            deleted.add(sheet(owner));
        }
        when(sheetRepository.findAllByUserIdAndDeletedTrueOrderByUpdatedAtDesc(owner)).thenReturn(deleted);

        service.delete(sheet.getId());

        assertTrue(sheet.isDeleted());
        // Вытесняется только самый старый лист — те, что между 20 и 30, остаются
        verify(sheetRepository).deleteAll(List.of(deleted.getLast()));
    }

    @Test
    void shareIssuesTokenAndKeepsItOnRepeatedCalls() {
        UUID owner = authenticate();
        CharacterSheet sheet = sheet(owner);
        when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));

        CharacterSheetShareResponse first = service.share(sheet.getId());
        CharacterSheetShareResponse second = service.share(sheet.getId());

        assertNotNull(first.getShareToken());
        // Идемпотентность: перевыпуск токена сломал бы уже разосланные ссылки
        assertEquals(first.getShareToken(), second.getShareToken());
        assertEquals(first.getShareToken(), sheet.getShareToken());
    }

    @Test
    void shareOnForeignSheetIsForbidden() {
        authenticate();
        CharacterSheet foreign = sheet(UUID.randomUUID());
        when(sheetRepository.findById(foreign.getId())).thenReturn(Optional.of(foreign));

        ApiException exception = assertThrows(ApiException.class, () -> service.share(foreign.getId()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertNull(foreign.getShareToken());
    }

    @Test
    void revokeShareClearsToken() {
        UUID owner = authenticate();
        CharacterSheet sheet = sheet(owner);
        sheet.setShareToken(UUID.randomUUID());
        when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));

        service.revokeShare(sheet.getId());

        assertNull(sheet.getShareToken());
    }

    @Test
    void findSharedReturnsSheetToAnonymousViewer() {
        CharacterSheet sheet = sheet(UUID.randomUUID());
        UUID token = UUID.randomUUID();
        sheet.setShareToken(token);
        CharacterSheetPublicResponse expected = new CharacterSheetPublicResponse();
        when(sheetRepository.findByShareTokenAndDeletedFalse(token)).thenReturn(Optional.of(sheet));
        when(sheetMapper.toPublicResponse(sheet)).thenReturn(expected);

        // Контекст безопасности пуст — ручка публичная
        CharacterSheetPublicResponse actual = service.findShared(token.toString());

        assertSame(expected, actual);
    }

    @Test
    void findSharedWithUnknownTokenIsNotFound() {
        UUID token = UUID.randomUUID();
        when(sheetRepository.findByShareTokenAndDeletedFalse(token)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findShared(token.toString()));
    }

    @Test
    void findSharedWithMalformedTokenIsNotFoundInsteadOfServerError() {
        assertThrows(EntityNotFoundException.class, () -> service.findShared("не-похоже-на-uuid"));
        assertThrows(EntityNotFoundException.class, () -> service.findShared("  "));

        verify(sheetRepository, never()).findByShareTokenAndDeletedFalse(any());
    }

    private static CharacterSheetRequest request() {
        CharacterSheetRequest request = new CharacterSheetRequest();
        request.setData(JsonNodeFactory.instance.objectNode());
        return request;
    }

    private static CharacterSheet sheet(UUID ownerId) {
        CharacterSheet sheet = new CharacterSheet();
        sheet.setId(UUID.randomUUID());
        sheet.setUserId(ownerId);
        sheet.setName("Гимли");
        sheet.setData(JsonNodeFactory.instance.objectNode());
        return sheet;
    }

    private static UUID authenticate() {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
        return user.getUuid();
    }
}
