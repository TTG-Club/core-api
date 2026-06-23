package club.ttg.dnd5.domain.subscription.rest.dto;

import club.ttg.dnd5.domain.subscription.model.SubscriptionType;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        SubscriptionType type,
        String status,
        String registrationCode,
        Integer durationMonths,
        String ownerUsername,
        Instant registeredAt,
        Instant startsAt,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt
) {
}
