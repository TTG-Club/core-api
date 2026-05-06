package club.ttg.dnd5.domain.user.rest.controller;

import club.ttg.dnd5.domain.user.rest.dto.migration.AuthUserMigrationResponse;
import club.ttg.dnd5.domain.user.service.LegacyUserMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users/migration")
@RequiredArgsConstructor
@Secured("ADMIN")
public class UserMigrationController
{
    private final LegacyUserMigrationService legacyUserMigrationService;

    @PostMapping("/auth-service")
    public AuthUserMigrationResponse migrateToAuthService(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    )
    {
        return legacyUserMigrationService.migrateToAuthService(authorization);
    }
}
