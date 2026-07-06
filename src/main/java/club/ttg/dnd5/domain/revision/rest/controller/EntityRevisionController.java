package club.ttg.dnd5.domain.revision.rest.controller;

import club.ttg.dnd5.domain.revision.model.EntityRevision;
import club.ttg.dnd5.domain.revision.rest.dto.RevisionDetailResponse;
import club.ttg.dnd5.domain.revision.rest.dto.RevisionResponse;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "История изменений", description = "Просмотр и откат версий сущностей")
@RestController
@RequestMapping("/api/v2/revisions")
@RequiredArgsConstructor
@Secured("ADMIN")
public class EntityRevisionController {

    private final EntityRevisionService revisionService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Список версий сущности")
    @GetMapping("/{entityType}/{entityId}")
    public List<RevisionResponse> getRevisions(@PathVariable String entityType,
                                               @PathVariable String entityId) {
        return revisionService.getRevisions(entityType, entityId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Operation(summary = "Снимок конкретной версии")
    @GetMapping("/{entityType}/{entityId}/{version}")
    public RevisionDetailResponse getRevision(@PathVariable String entityType,
                                              @PathVariable String entityId,
                                              @PathVariable int version) {
        return toDetail(revisionService.getRevision(entityType, entityId, version));
    }

    @Operation(summary = "Откатить сущность к версии",
            description = "Применяет снимок выбранной версии. Откат сам сохраняется как новая версия.")
    @PostMapping("/{entityType}/{entityId}/revert/{version}")
    public void revert(@PathVariable String entityType,
                       @PathVariable String entityId,
                       @PathVariable int version) {
        revisionService.revert(entityType, entityId, version);
    }

    private RevisionResponse toResponse(EntityRevision revision) {
        return new RevisionResponse(
                revision.getId(),
                revision.getVersion(),
                revision.getOperation(),
                revision.getChangedBy(),
                revision.getChangedAt(),
                revision.getHash());
    }

    private RevisionDetailResponse toDetail(EntityRevision revision) {
        try {
            return new RevisionDetailResponse(
                    revision.getId(),
                    revision.getEntityType(),
                    revision.getEntityId(),
                    revision.getVersion(),
                    revision.getOperation(),
                    revision.getChangedBy(),
                    revision.getChangedAt(),
                    revision.getHash(),
                    objectMapper.readTree(revision.getSnapshot()));
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось прочитать снимок версии");
        }
    }
}
