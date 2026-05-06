package club.ttg.dnd5.domain.user.service;

import club.ttg.dnd5.domain.user.model.Role;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.repository.UserRepository;
import club.ttg.dnd5.domain.user.rest.dto.migration.AuthLegacyUserImportRequest;
import club.ttg.dnd5.domain.user.rest.dto.migration.AuthLegacyUserImportResponse;
import club.ttg.dnd5.domain.user.rest.dto.migration.AuthUserMigrationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LegacyUserMigrationService
{
    private static final boolean LEGACY_USERS_EMAIL_VERIFIED = true;

    private final UserRepository userRepository;
    private final RestClient.Builder restClientBuilder;

    @Value("${auth-service.base-url}")
    private String authServiceBaseUrl;

    @Value("${auth-service.migration.batch-size:100}")
    private int batchSize;

    public AuthUserMigrationResponse migrateToAuthService(String authorization)
    {
        List<User> legacyUsers = userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        RestClient authServiceClient = restClientBuilder
                .baseUrl(authServiceBaseUrl)
                .build();

        List<AuthLegacyUserImportResponse> responses = new ArrayList<>();
        int importBatchSize = Math.max(1, batchSize);
        for (int start = 0; start < legacyUsers.size(); start += importBatchSize)
        {
            int end = Math.min(start + importBatchSize, legacyUsers.size());
            List<AuthLegacyUserImportRequest> batch = legacyUsers.subList(start, end)
                    .stream()
                    .map(this::toImportRequest)
                    .toList();

            List<AuthLegacyUserImportResponse> imported = authServiceClient.post()
                    .uri("/api/admin/users/import/legacy")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .body(batch)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>()
                    {
                    });

            if (imported != null)
            {
                responses.addAll(imported);
            }
        }

        int created = (int) responses.stream()
                .filter(AuthLegacyUserImportResponse::created)
                .count();

        return new AuthUserMigrationResponse(
                legacyUsers.size(),
                created,
                responses.size() - created,
                responses
        );
    }

    private AuthLegacyUserImportRequest toImportRequest(User user)
    {
        return new AuthLegacyUserImportRequest(
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                LEGACY_USERS_EMAIL_VERIFIED,
                user.getCreatedAt(),
                roleNames(user.getRoles())
        );
    }

    private Set<String> roleNames(Collection<Role> roles)
    {
        if (roles == null)
        {
            return Set.of();
        }

        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
