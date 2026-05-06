package club.ttg.dnd5.domain.user.rest.dto.migration;

import java.util.Set;
import java.util.UUID;

public record AuthLegacyUserImportResponse(
        UUID id,
        String username,
        String email,
        boolean created,
        Set<String> roles
)
{
}
