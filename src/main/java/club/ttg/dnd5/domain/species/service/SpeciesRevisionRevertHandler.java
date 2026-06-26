package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.revision.service.RevisionRevertHandler;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesRequest;
import club.ttg.dnd5.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpeciesRevisionRevertHandler implements RevisionRevertHandler {

    private final SpeciesService speciesService;
    private final ObjectMapper objectMapper;

    @Override
    public String entityType() {
        return SpeciesService.REVISION_ENTITY_TYPE;
    }

    @Override
    public void revert(String entityId, String snapshotJson) {
        SpeciesRequest request;
        try {
            request = objectMapper.readValue(snapshotJson, SpeciesRequest.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось прочитать снимок вида для отката");
        }
        speciesService.update(entityId, request);
    }
}
