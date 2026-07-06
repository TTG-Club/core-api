package club.ttg.dnd5.domain.revision.rest.dto;

import club.ttg.dnd5.domain.revision.model.RevisionOperation;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

/**
 * Полная версия со снимком сущности (JSON в форме редактора).
 */
public record RevisionDetailResponse(
        Long id,
        String entityType,
        String entityId,
        int version,
        RevisionOperation operation,
        String changedBy,
        Instant changedAt,
        String hash,
        JsonNode snapshot
) {
}
