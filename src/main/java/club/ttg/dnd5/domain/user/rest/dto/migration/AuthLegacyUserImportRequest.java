package club.ttg.dnd5.domain.user.rest.dto.migration;

import java.time.Instant;
import java.util.Set;

public record AuthLegacyUserImportRequest(
        String username,
        String email,
        String passwordHash,
        boolean enabled,
        boolean emailVerified,
        Instant createdAt,
        Set<String> roles
)
{
}
