package club.ttg.dnd5.domain.user.service;

import club.ttg.dnd5.domain.user.model.UserDisplayName;
import club.ttg.dnd5.domain.user.repository.UserDisplayNameRepository;
import club.ttg.dnd5.domain.user.rest.dto.DisplayNameByUserIdResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Резолв «UUID -> отображаемое имя»: его читают сервисы, которые логинов не хранят
 * (find-game-api подписывает мастера и игроков в карточке игры и в чате).
 */
class DisplayNameServiceResolveByUserIdsTest {
    private final UserDisplayNameRepository repository = mock(UserDisplayNameRepository.class);
    private final DisplayNameGenerator generator = mock(DisplayNameGenerator.class);
    private final DisplayNameService service = new DisplayNameService(repository, generator);

    @Test
    void resolvesKnownUserIds() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        when(repository.findAllById(anyIterable()))
                .thenReturn(List.of(entity(first, "Ворчливый Гоблин"), entity(second, "Тихий Друид")));

        List<DisplayNameByUserIdResponse> resolved = service.resolveByUserIds(List.of(first, second));

        assertEquals(2, resolved.size());
        assertTrue(resolved.contains(new DisplayNameByUserIdResponse(first, "Ворчливый Гоблин")));
        assertTrue(resolved.contains(new DisplayNameByUserIdResponse(second, "Тихий Друид")));
    }

    @Test
    void unknownUserIdsAreOmittedInsteadOfReported() {
        UUID known = UUID.randomUUID();
        UUID unknown = UUID.randomUUID();
        when(repository.findAllById(anyIterable())).thenReturn(List.of(entity(known, "Ворчливый Гоблин")));

        List<DisplayNameByUserIdResponse> resolved = service.resolveByUserIds(List.of(known, unknown));

        // Пропуск не подтверждает существование пользователя — вызывающий сам решает,
        // чем заменить отсутствующее имя.
        assertEquals(List.of(new DisplayNameByUserIdResponse(known, "Ворчливый Гоблин")), resolved);
    }

    @Test
    void emptyAndNullInputDoNotTouchRepository() {
        assertTrue(service.resolveByUserIds(null).isEmpty());
        assertTrue(service.resolveByUserIds(List.of()).isEmpty());
        assertTrue(service.resolveByUserIds(Collections.singletonList(null)).isEmpty());

        verify(repository, never()).findAllById(anyIterable());
    }

    @Test
    void lookupSizeIsCappedAndDuplicatesCollapse() {
        List<UUID> requested = new ArrayList<>(
                IntStream.range(0, 250).mapToObj(index -> UUID.randomUUID()).toList());
        UUID duplicated = requested.get(0);
        requested.add(duplicated);
        when(repository.findAllById(anyIterable())).thenReturn(List.of());

        service.resolveByUserIds(requested);

        ArgumentCaptor<Iterable<UUID>> captor = ArgumentCaptor.captor();
        verify(repository).findAllById(captor.capture());
        Collection<UUID> passed = (Collection<UUID>) captor.getValue();
        assertTrue(passed.size() <= 200, "Размер выборки должен быть ограничен 200 идентификаторами");
    }

    private UserDisplayName entity(UUID userId, String displayName) {
        UserDisplayName entity = new UserDisplayName();
        entity.setUserId(userId);
        entity.setUsername("user-" + userId);
        entity.setDisplayName(displayName);
        return entity;
    }
}
