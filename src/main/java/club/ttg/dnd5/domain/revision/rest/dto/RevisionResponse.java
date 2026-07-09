package club.ttg.dnd5.domain.revision.rest.dto;

import club.ttg.dnd5.domain.revision.model.RevisionOperation;

import java.time.Instant;

/**
 * Метаданные одной версии (без тела снимка) для списка истории.
 */
public record RevisionResponse(
        Long id,
        int version,
        RevisionOperation operation,
        String changedBy,
        Instant changedAt,
        String hash
) {
}
