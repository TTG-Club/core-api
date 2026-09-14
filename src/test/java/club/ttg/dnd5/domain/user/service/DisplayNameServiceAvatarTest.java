package club.ttg.dnd5.domain.user.service;

import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.model.UserDisplayName;
import club.ttg.dnd5.domain.user.repository.UserDisplayNameRepository;
import club.ttg.dnd5.domain.user.rest.dto.AvatarResponse;
import club.ttg.dnd5.domain.user.rest.dto.DisplayNameByLoginResponse;
import club.ttg.dnd5.domain.user.rest.dto.DisplayNameByUserIdResponse;
import club.ttg.dnd5.domain.user.rest.dto.DisplayNameResponse;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Аватарка пользователя: сам файл кладёт в S3 core-app, core-api хранит ссылку и следит,
 * чтобы она вела в папку этого пользователя — иначе прямым запросом можно поставить себе
 * чужую картинку или картинку с внешнего сайта.
 */
class DisplayNameServiceAvatarTest {
    private final UserDisplayNameRepository repository = mock(UserDisplayNameRepository.class);
    private final DisplayNameGenerator generator = mock(DisplayNameGenerator.class);
    private final DisplayNameService service = new DisplayNameService(repository, generator);

    private UUID userId;

    @BeforeEach
    void authenticate() {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setUsername("goblin");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
        userId = user.getUuid();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownAvatarUrlIsStored() {
        UserDisplayName existing = entity(userId, "Ворчливый Гоблин", null);
        when(repository.findById(userId)).thenReturn(Optional.of(existing));
        String avatarUrl = ownUrl("1757851200000-avatar.webp");

        AvatarResponse response = service.updateAvatarForCurrentUser(avatarUrl);

        assertEquals(new AvatarResponse(avatarUrl), response);
        UserDisplayName saved = lastSaved();
        assertEquals(avatarUrl, saved.getAvatarUrl());
        assertEquals("Ворчливый Гоблин", saved.getDisplayName());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void repeatedPutReplacesAvatar() {
        UserDisplayName existing = entity(userId, "Ворчливый Гоблин", ownUrl("1757851200000-avatar.webp"));
        existing.setUpdatedAt(Instant.EPOCH);
        when(repository.findById(userId)).thenReturn(Optional.of(existing));
        String replacement = ownUrl("1757851300000-avatar.webp");

        AvatarResponse response = service.updateAvatarForCurrentUser(replacement);

        assertEquals(new AvatarResponse(replacement), response);
        UserDisplayName saved = lastSaved();
        assertEquals(replacement, saved.getAvatarUrl());
        assertTrue(saved.getUpdatedAt().isAfter(Instant.EPOCH));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/s3/avatars/<other>/a.webp",
            "https://evil.example/a.webp",
            "https://evil.example/s3/avatars/<sub>/a.webp",
            "/s3/avatars/<SUB>/a.webp",
            "/s3/avatars/<sub>/../x.webp",
            "/s3/avatars/<sub>/a.png",
            "/s3/avatars/<sub>/a/b.webp",
            "/s3/avatars/<sub>/a.webp?x=1",
            "/s3/avatars/<sub>/a.webp\n",
            "/s3/avatars/<sub>/.webp",
            "/s3/other/<sub>/a.webp"
    })
    void foreignOrMalformedUrlIsRejected(String template) {
        String avatarUrl = template
                .replace("<sub>", userId.toString())
                .replace("<SUB>", userId.toString().toUpperCase(Locale.ROOT))
                .replace("<other>", UUID.randomUUID().toString());

        ApiException ex = assertThrows(ApiException.class, () -> service.updateAvatarForCurrentUser(avatarUrl));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatus());
        assertEquals("Недопустимая ссылка на аватарку", ex.getMessage());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void putWithoutRecordCreatesItWithRandomName() {
        when(repository.findById(userId)).thenReturn(Optional.empty());
        when(generator.nextBaseName()).thenReturn("Тихий Друид");
        String avatarUrl = ownUrl("1757851200000-avatar.webp");

        AvatarResponse response = service.updateAvatarForCurrentUser(avatarUrl);

        assertEquals(new AvatarResponse(avatarUrl), response);
        UserDisplayName saved = lastSaved();
        assertEquals(userId, saved.getUserId());
        assertEquals("goblin", saved.getUsername());
        assertEquals("Тихий Друид", saved.getDisplayName());
        assertEquals(avatarUrl, saved.getAvatarUrl());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void deleteWithoutRecordSucceedsAndDoesNotCreateIt() {
        when(repository.findById(userId)).thenReturn(Optional.empty());

        AvatarResponse response = service.deleteAvatarForCurrentUser();

        assertEquals(new AvatarResponse(null), response);
        verify(repository, never()).saveAndFlush(any());
        verify(repository, never()).save(any());
    }

    @Test
    void deleteWithoutAvatarWritesNothing() {
        when(repository.findById(userId)).thenReturn(Optional.of(entity(userId, "Ворчливый Гоблин", null)));

        assertEquals(new AvatarResponse(null), service.deleteAvatarForCurrentUser());

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deleteClearsExistingAvatar() {
        UserDisplayName existing = entity(userId, "Ворчливый Гоблин", ownUrl("1757851200000-avatar.webp"));
        existing.setUpdatedAt(Instant.EPOCH);
        when(repository.findById(userId)).thenReturn(Optional.of(existing));

        assertEquals(new AvatarResponse(null), service.deleteAvatarForCurrentUser());

        UserDisplayName saved = lastSaved();
        assertNull(saved.getAvatarUrl());
        assertEquals("Ворчливый Гоблин", saved.getDisplayName());
        assertTrue(saved.getUpdatedAt().isAfter(Instant.EPOCH));
    }

    @Test
    void ownDisplayNameCarriesAvatar() {
        String avatarUrl = ownUrl("1757851200000-avatar.webp");
        when(repository.findById(userId)).thenReturn(Optional.of(entity(userId, "Ворчливый Гоблин", avatarUrl)));

        assertEquals(new DisplayNameResponse("Ворчливый Гоблин", avatarUrl), service.getOrCreateForCurrentUser());
    }

    @Test
    void lazilyCreatedDisplayNameHasNoAvatar() {
        when(repository.findById(userId)).thenReturn(Optional.empty());
        when(generator.nextBaseName()).thenReturn("Тихий Друид");

        assertEquals(new DisplayNameResponse("Тихий Друид", null), service.getOrCreateForCurrentUser());
    }

    @Test
    void renameKeepsCurrentAvatarInResponse() {
        String avatarUrl = ownUrl("1757851200000-avatar.webp");
        when(repository.findById(userId)).thenReturn(Optional.of(entity(userId, "Ворчливый Гоблин", avatarUrl)));

        DisplayNameResponse response = service.updateForCurrentUser("Тихий Друид");

        assertEquals(new DisplayNameResponse("Тихий Друид", avatarUrl), response);
        assertEquals(avatarUrl, lastSaved().getAvatarUrl());
    }

    @Test
    void resolveByUserIdsReturnsAvatar() {
        UUID other = UUID.randomUUID();
        String avatarUrl = ownUrl("1757851200000-avatar.webp");
        when(repository.findAllById(anyIterable())).thenReturn(List.of(
                entity(userId, "Ворчливый Гоблин", avatarUrl),
                entity(other, "Тихий Друид", null)));

        List<DisplayNameByUserIdResponse> resolved = service.resolveByUserIds(List.of(userId, other));

        assertEquals(2, resolved.size());
        assertTrue(resolved.contains(new DisplayNameByUserIdResponse(userId, "Ворчливый Гоблин", avatarUrl)));
        assertTrue(resolved.contains(new DisplayNameByUserIdResponse(other, "Тихий Друид", null)));
    }

    @Test
    void resolveByLoginsReturnsAvatar() {
        String avatarUrl = ownUrl("1757851200000-avatar.webp");
        when(repository.findAllByUsernameLowerIn(anyCollection()))
                .thenReturn(List.of(entity(userId, "Ворчливый Гоблин", avatarUrl)));

        List<DisplayNameByLoginResponse> resolved = service.resolveByLogins(List.of("Goblin"));

        assertEquals(List.of(new DisplayNameByLoginResponse("user-" + userId, "Ворчливый Гоблин", avatarUrl)),
                resolved);
    }

    private String ownUrl(String fileName) {
        return "/s3/avatars/" + userId + "/" + fileName;
    }

    private UserDisplayName lastSaved() {
        ArgumentCaptor<UserDisplayName> captor = ArgumentCaptor.forClass(UserDisplayName.class);
        verify(repository, atLeastOnce()).saveAndFlush(captor.capture());
        return captor.getValue();
    }

    private UserDisplayName entity(UUID id, String displayName, String avatarUrl) {
        UserDisplayName entity = new UserDisplayName();
        entity.setUserId(id);
        entity.setUsername("user-" + id);
        entity.setDisplayName(displayName);
        entity.setAvatarUrl(avatarUrl);
        return entity;
    }
}
