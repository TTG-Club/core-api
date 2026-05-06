package club.ttg.dnd5.domain.user.rest.dto.migration;

import java.util.List;

public record AuthUserMigrationResponse(
        int total,
        int created,
        int skipped,
        List<AuthLegacyUserImportResponse> users
)
{
}
