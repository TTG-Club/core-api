package club.ttg.dnd5.domain.vttg.rest.controller;

import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgCompendiumVersionRequest;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgCompendiumVersionResponse;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgRebuildStatus;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgRebuildStatus.State;
import club.ttg.dnd5.domain.vttg.service.VttgCompendiumRebuildService;
import club.ttg.dnd5.domain.vttg.service.VttgCompendiumVersionService;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VttgAdminControllerTest {

    private final VttgCompendiumVersionService versionService = mock(VttgCompendiumVersionService.class);
    private final VttgCompendiumRebuildService rebuildService = mock(VttgCompendiumRebuildService.class);
    private final VttgAdminController controller = new VttgAdminController(versionService, rebuildService);

    @BeforeEach
    void signInAsAdmin() {
        User admin = new User();
        admin.setUsername("admin");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(admin, null, List.of()));
    }

    @AfterEach
    void signOut() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void putRaisesVersionEvictsCacheAndStartsRebuild() {
        Instant now = Instant.parse("2026-09-26T10:00:00Z");
        VttgRebuildStatus running = new VttgRebuildStatus(State.RUNNING, now, null, null);
        when(versionService.raise(36, "admin"))
                .thenReturn(new VttgCompendiumVersionService.Snapshot(36, now, "admin", 0));
        when(rebuildService.request()).thenReturn(running);

        VttgCompendiumVersionResponse response =
                controller.updateCompendiumVersion(new VttgCompendiumVersionRequest(36));

        assertEquals(new VttgCompendiumVersionResponse(36, now, "admin", running), response);
        var order = inOrder(versionService, rebuildService);
        order.verify(versionService).raise(36, "admin");
        order.verify(rebuildService).evictFullExport();
        order.verify(rebuildService).request();
    }

    @Test
    void putWithVersionNotGreaterIsConflictWithoutRebuild() {
        when(versionService.raise(35, "admin"))
                .thenThrow(new ApiException(HttpStatus.CONFLICT, "Версия должна быть больше текущей (35)"));

        ApiException conflict = assertThrows(ApiException.class,
                () -> controller.updateCompendiumVersion(new VttgCompendiumVersionRequest(35)));

        assertEquals(HttpStatus.CONFLICT, conflict.getStatus());
        verify(rebuildService, never()).evictFullExport();
        verify(rebuildService, never()).request();
    }

    @Test
    void getReturnsVersionAndRebuildStatus() {
        when(versionService.get()).thenReturn(new VttgCompendiumVersionService.Snapshot(35, null, null, 0));
        when(rebuildService.status()).thenReturn(VttgRebuildStatus.IDLE);

        assertEquals(new VttgCompendiumVersionResponse(35, null, null, VttgRebuildStatus.IDLE),
                controller.getCompendiumVersion());
    }
}
